package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"os"
	"sync/atomic"
	"time"

	"infinispan.org/go-client/hotrod"
	"infinispan.org/go-client/internal/codec"
	"infinispan.org/go-client/internal/protostream"
)

const instaPostProto = `syntax = "proto3";
package tutorial;

message InstaPost {
    string id = 1;
    string user = 2;
    string hashtag = 3;
}`

const cacheConfig = `<distributed-cache name="test">
    <encoding media-type="application/x-protostream"/>
</distributed-cache>`

var users = []string{
	"gustavoalle", "remerson", "anistor", "karesti", "ttarrant",
	"belen_esteban", "dberindei", "galderz", "wburns", "pruivo",
	"oliveira", "vrigamonti",
}

var hashtags = []string{
	"love", "instagood", "photooftheday", "fashion", "beautiful",
	"happy", "cute", "tbt", "like4like", "followme", "infinispan",
}

func encodeInstaPost(id, user, hashtag string) []byte {
	var dst []byte
	dst = appendLenDelimited(dst, 1, []byte(id))
	dst = appendLenDelimited(dst, 2, []byte(user))
	dst = appendLenDelimited(dst, 3, []byte(hashtag))
	return dst
}

func main() {
	uri := os.Getenv("INFINISPAN_URI")
	if uri == "" {
		uri = "hotrod://admin:password@localhost:11222"
	}

	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()

	client, err := hotrod.NewClient(ctx, uri, hotrod.WithClientIntelligence(hotrod.IntelligenceBasic))
	if err != nil {
		log.Fatalf("NewClient: %v", err)
	}
	defer client.Close()

	// Register the InstaPost proto schema
	if err := client.Schemas().Register(ctx, "instapost.proto", instaPostProto); err != nil {
		log.Fatalf("Register schema: %v", err)
	}
	fmt.Println("Registered instapost.proto schema.")

	if err := client.Administration().GetOrCreateCache(ctx, "test", cacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}

	cache := client.Cache("test")

	// Register a continuous query filtering posts by user "belen_esteban"
	cq, err := cache.ContinuousQuery(ctx,
		"FROM tutorial.InstaPost p WHERE p.user = :userName",
		hotrod.WithCQParam("userName", "belen_esteban"),
	)
	if err != nil {
		log.Fatalf("ContinuousQuery: %v", err)
	}
	fmt.Println("Continuous query registered.")

	// Consume CQ events in a goroutine
	var matchCount atomic.Int64
	done := make(chan struct{})
	go func() {
		defer close(done)
		for event := range cq.Events {
			if event.Type == hotrod.CQJoining {
				hashtag := decodeInstaPostHashtag(event.Value)
				fmt.Printf("@belen_esteban has posted again! Hashtag: #%s\n", hashtag)
				matchCount.Add(1)
			}
		}
	}()

	// Add 100 random posts
	numPosts := 100
	for i := 0; i < numPosts; i++ {
		id := fmt.Sprintf("post-%d", i)
		user := users[rand.Intn(len(users))]
		hashtag := hashtags[rand.Intn(len(hashtags))]

		keyBytes := protostream.WrapString(id)
		valBytes := protostream.WrapMessage(encodeInstaPost(id, user, hashtag), "tutorial.InstaPost")
		if err := cache.PutRaw(ctx, keyBytes, valBytes, codec.MediaIDProtostream); err != nil {
			log.Fatalf("Put: %v", err)
		}
		time.Sleep(10 * time.Millisecond)
	}

	// Wait briefly for remaining events
	time.Sleep(time.Second)

	// Remove the continuous query
	if err := cache.RemoveContinuousQuery(ctx, cq); err != nil {
		log.Fatalf("RemoveContinuousQuery: %v", err)
	}
	<-done

	fmt.Printf("\nTotal posts: %d\n", numPosts)
	fmt.Printf("Total posts by @belen_esteban: %d\n", matchCount.Load())

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
}

func decodeInstaPostHashtag(data []byte) string {
	hashtag, _ := readStringField(data, 3)
	return hashtag
}

func readStringField(data []byte, targetField int) (string, bool) {
	var result string
	var found bool
	scanFields(data, func(fn, wt int, val []byte) {
		if fn == targetField && wt == 2 {
			result = string(val)
			found = true
		}
	})
	return result, found
}

// --- Protobuf encoding/decoding helpers ---

func scanFields(data []byte, fn func(fieldNumber, wireType int, value []byte)) {
	pos := 0
	for pos < len(data) {
		tag, n := decodeUvarint(data[pos:])
		if n <= 0 {
			return
		}
		pos += n
		fieldNumber := int(tag >> 3)
		wireType := int(tag & 0x7)

		switch wireType {
		case 0: // varint
			start := pos
			for pos < len(data) && data[pos] >= 0x80 {
				pos++
			}
			if pos < len(data) {
				pos++
			}
			fn(fieldNumber, wireType, data[start:pos])
		case 1: // fixed64
			if pos+8 <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+8])
				pos += 8
			}
		case 2: // length-delimited
			length, n := decodeUvarint(data[pos:])
			if n <= 0 {
				return
			}
			pos += n
			if pos+int(length) <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+int(length)])
				pos += int(length)
			}
		case 5: // fixed32
			if pos+4 <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+4])
				pos += 4
			}
		default:
			return
		}
	}
}

func decodeUvarint(data []byte) (uint64, int) {
	var v uint64
	for i, b := range data {
		if i >= 10 {
			return 0, -1
		}
		v |= uint64(b&0x7F) << (7 * i)
		if b < 0x80 {
			return v, i + 1
		}
	}
	return 0, -1
}

func appendTag(dst []byte, fieldNumber int, wireType int) []byte {
	return appendUvarint(dst, uint64(fieldNumber<<3|wireType))
}

func appendLenDelimited(dst []byte, fieldNumber int, data []byte) []byte {
	dst = appendTag(dst, fieldNumber, 2)
	dst = appendUvarint(dst, uint64(len(data)))
	dst = append(dst, data...)
	return dst
}

func appendUvarint(dst []byte, v uint64) []byte {
	for v >= 0x80 {
		dst = append(dst, byte(v)|0x80)
		v >>= 7
	}
	dst = append(dst, byte(v))
	return dst
}
