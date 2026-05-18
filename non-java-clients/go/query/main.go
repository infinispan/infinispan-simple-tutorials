package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	"infinispan.org/go-client/internal/codec"
	"infinispan.org/go-client/internal/protostream"
)

const personProto = `syntax = "proto3";
package tutorial;

/* @Indexed */
message Person {
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string firstName = 1;
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string lastName = 2;
    /* @Basic(projectable = true, sortable = true) */
    int32 bornYear = 3;
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string bornIn = 4;
}`

const indexedCacheConfig = `<distributed-cache statistics="true">
    <encoding>
        <key media-type="application/x-protostream"/>
        <value media-type="application/x-protostream"/>
    </encoding>
    <indexing enabled="true" storage="local-heap">
        <indexed-entities>
            <indexed-entity>tutorial.Person</indexed-entity>
        </indexed-entities>
    </indexing>
</distributed-cache>`

type person struct {
	firstName string
	lastName  string
	bornYear  int32
	bornIn    string
}

func encodePerson(p person) []byte {
	var dst []byte
	dst = appendLenDelimited(dst, 1, []byte(p.firstName))
	dst = appendLenDelimited(dst, 2, []byte(p.lastName))
	dst = appendVarintField(dst, 3, uint64(p.bornYear))
	dst = appendLenDelimited(dst, 4, []byte(p.bornIn))
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

	// Register the Person proto schema
	if err := client.Schemas().Register(ctx, "person.proto", personProto); err != nil {
		log.Fatalf("Register schema: %v", err)
	}
	fmt.Println("Registered person.proto schema.")

	// Create the indexed cache
	if err := client.Administration().GetOrCreateCache(ctx, "indexedPeopleCache", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'indexedPeopleCache' ready.")

	cache := client.Cache("indexedPeopleCache")
	addData(ctx, cache)

	queryAll(ctx, cache)
	queryWithWhere(ctx, cache)
	queryWithProjection(ctx, cache)

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "indexedPeopleCache"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}

func addData(ctx context.Context, cache *hotrod.RemoteCache) {
	people := map[string]person{
		"hgranger": {"Hermione", "Granger", 1990, "London"},
		"hpotter":  {"Harry", "Potter", 1991, "Godric's Hollow"},
		"rwesley":  {"Ron", "Wesley", 1990, "London"},
		"dmalfoy":  {"Draco", "Malfoy", 1989, "London"},
	}
	for key, p := range people {
		keyBytes := protostream.WrapString(key)
		valBytes := protostream.WrapMessage(encodePerson(p), "tutorial.Person")
		if err := cache.PutRaw(ctx, keyBytes, valBytes, codec.MediaIDProtostream); err != nil {
			log.Fatalf("Put %s: %v", key, err)
		}
	}
	fmt.Printf("Added %d people.\n\n", 4)
}

func queryAll(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Query all ===")
	result, err := cache.Query(ctx, "from tutorial.Person")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Total results: %d\n", result.NumResults)
	for _, entry := range result.Entries {
		firstName, _ := readStringField(entry.Value, 1)
		lastName, _ := readStringField(entry.Value, 2)
		fmt.Printf("  %s %s\n", firstName, lastName)
	}
	fmt.Println()
}

func queryWithWhere(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Query: people with lastName = 'Granger' ===")
	result, err := cache.Query(ctx, "from tutorial.Person p where p.lastName = :lastName",
		hotrod.WithQueryParam("lastName", "Granger"),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		firstName, _ := readStringField(entry.Value, 1)
		lastName, _ := readStringField(entry.Value, 2)
		fmt.Printf("  %s %s\n", firstName, lastName)
	}
	fmt.Println()
}

func queryWithProjection(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Query: projection of people born in London ===")
	result, err := cache.Query(ctx, "select p.firstName, p.lastName from tutorial.Person p where p.bornIn = 'London'")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  %s %s\n", entry.Projections[0], entry.Projections[1])
	}
	fmt.Println()
}

// --- Protobuf encoding/decoding helpers ---

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

func appendVarintField(dst []byte, fieldNumber int, v uint64) []byte {
	dst = appendTag(dst, fieldNumber, 0)
	dst = appendUvarint(dst, v)
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
