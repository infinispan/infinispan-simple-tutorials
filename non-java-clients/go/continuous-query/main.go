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
	pb "infinispan.org/tutorials/go/continuous-query/proto"
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

	if err := client.Schemas().Register(ctx, "instapost.proto", instaPostProto); err != nil {
		log.Fatalf("Register schema: %v", err)
	}
	fmt.Println("Registered instapost.proto schema.")

	if err := client.Admin().GetOrCreateCache(ctx, "test", cacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}

	cache := hotrod.NewTypedCache[string, *pb.InstaPost](client, "test").Build()

	cq, err := cache.ContinuousQuery(ctx,
		"FROM tutorial.InstaPost p WHERE p.user = :userName",
		hotrod.WithCQParam("userName", "belen_esteban"),
	)
	if err != nil {
		log.Fatalf("ContinuousQuery: %v", err)
	}
	fmt.Println("Continuous query registered.")

	var matchCount atomic.Int64
	done := make(chan struct{})
	go func() {
		defer close(done)
		for event := range cq.Events {
			if event.Type == hotrod.CQJoining {
				fmt.Printf("@belen_esteban has posted again! Hashtag: #%s\n", event.Value.Hashtag)
				matchCount.Add(1)
			}
		}
	}()

	numPosts := 100
	for i := range numPosts {
		id := fmt.Sprintf("post-%d", i)
		user := users[rand.Intn(len(users))]
		hashtag := hashtags[rand.Intn(len(hashtags))]

		if err := cache.Put(ctx, id, &pb.InstaPost{Id: id, User: user, Hashtag: hashtag}); err != nil {
			log.Fatalf("Put: %v", err)
		}
		time.Sleep(10 * time.Millisecond)
	}

	time.Sleep(time.Second)

	if err := cache.RemoveContinuousQuery(ctx, cq); err != nil {
		log.Fatalf("RemoveContinuousQuery: %v", err)
	}
	<-done

	fmt.Printf("\nTotal posts: %d\n", numPosts)
	fmt.Printf("Total posts by @belen_esteban: %d\n", matchCount.Load())

	if err := client.Admin().RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
}
