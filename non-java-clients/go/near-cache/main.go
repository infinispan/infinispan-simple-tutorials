package main

import (
	"context"
	"fmt"
	"log"
	"math/rand"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const testCacheConfig = `<distributed-cache name="testCache">
    <encoding media-type="application/x-protostream"/>
</distributed-cache>`

const nearCacheConfigXML = `<distributed-cache name="testCacheNearCaching">
    <encoding media-type="application/x-protostream"/>
</distributed-cache>`

func main() {
	uri := os.Getenv("INFINISPAN_URI")
	if uri == "" {
		uri = "hotrod://admin:password@localhost:11222"
	}

	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()

	client, err := hotrod.NewClient(ctx, uri)
	if err != nil {
		log.Fatalf("NewClient: %v", err)
	}
	defer client.Close()

	admin := client.Administration()

	if err := admin.GetOrCreateCache(ctx, "testCache", testCacheConfig); err != nil {
		log.Fatalf("Create testCache: %v", err)
	}
	if err := admin.GetOrCreateCache(ctx, "testCacheNearCaching", nearCacheConfigXML); err != nil {
		log.Fatalf("Create testCacheNearCaching: %v", err)
	}

	testCache := client.Cache("testCache")

	// Create a near cache with max 20 entries
	nearCache, err := hotrod.NewNearCache(ctx, client, "testCacheNearCaching",
		hotrod.WithMaxNearCacheEntries(20),
	)
	if err != nil {
		log.Fatalf("NewNearCache: %v", err)
	}

	// Populate both caches with 20 entries
	for i := 1; i <= 20; i++ {
		key := []byte(fmt.Sprintf("%d", i))
		val := []byte(fmt.Sprintf("value-%d", i))
		if err := testCache.Put(ctx, key, val); err != nil {
			log.Fatalf("Put testCache: %v", err)
		}
		if err := nearCache.Put(ctx, key, val); err != nil {
			log.Fatalf("Put nearCache: %v", err)
		}
	}

	// Read 10,000 random entries from the regular cache
	start := time.Now()
	for i := 0; i < 10_000; i++ {
		key := []byte(fmt.Sprintf("%d", rand.Intn(20)+1))
		testCache.Get(ctx, key)
	}
	fmt.Printf("Time to complete 10,000 reads with regular cache:    %v\n", time.Since(start))

	// Read 10,000 random entries from the near cache
	start = time.Now()
	for i := 0; i < 10_000; i++ {
		key := []byte(fmt.Sprintf("%d", rand.Intn(20)+1))
		nearCache.Get(ctx, key)
	}
	fmt.Printf("Time to complete 10,000 reads with near cache:       %v\n", time.Since(start))

	// Clean up
	if err := nearCache.Close(ctx); err != nil {
		log.Fatalf("Close nearCache: %v", err)
	}
	if err := admin.RemoveCache(ctx, "testCache"); err != nil {
		log.Fatalf("RemoveCache testCache: %v", err)
	}
	if err := admin.RemoveCache(ctx, "testCacheNearCaching"); err != nil {
		log.Fatalf("RemoveCache testCacheNearCaching: %v", err)
	}
	fmt.Println("Caches removed.")
}
