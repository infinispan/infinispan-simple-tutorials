package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const cacheConfig = `<distributed-cache name="test">
    <encoding media-type="application/x-protostream"/>
</distributed-cache>`

func main() {
	uri := os.Getenv("INFINISPAN_URI")
	if uri == "" {
		uri = "hotrod://admin:password@localhost:11222"
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	client, err := hotrod.NewClient(ctx, uri)
	if err != nil {
		log.Fatalf("NewClient: %v", err)
	}
	defer client.Close()

	if err := client.Administration().GetOrCreateCache(ctx, "test", cacheConfig); err != nil {
		log.Fatalf("GetOrCreateCache: %v", err)
	}
	fmt.Println("Cache 'test' ready.")

	cache := client.Cache("test")

	// Store a value
	if err := cache.Put(ctx, []byte("key"), []byte("value")); err != nil {
		log.Fatalf("Put: %v", err)
	}

	// Retrieve the value and print it
	val, found, err := cache.Get(ctx, []byte("key"))
	if err != nil {
		log.Fatalf("Get: %v", err)
	}
	if found {
		fmt.Printf("key = %s\n", val)
	}

	// Remove the cache
	if err := client.Administration().RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}
