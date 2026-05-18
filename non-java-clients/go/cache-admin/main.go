package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const xmlConfig = `<distributed-cache name="CacheWithXMLConfiguration" mode="SYNC">
    <encoding media-type="application/x-protostream"/>
    <locking isolation="READ_COMMITTED"/>
    <transaction mode="NON_XA"/>
    <expiration lifespan="60000" interval="20000"/>
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

	admin := client.Administration()

	// Create a simple cache with no specific configuration
	createSimpleCache(ctx, admin)

	// Create a cache with full XML configuration
	createCacheWithXMLConfiguration(ctx, admin)

	// Clean up
	if err := admin.RemoveCache(ctx, "SimpleCache"); err != nil {
		log.Fatalf("RemoveCache SimpleCache: %v", err)
	}
	if err := admin.RemoveCache(ctx, "CacheWithXMLConfiguration"); err != nil {
		log.Fatalf("RemoveCache CacheWithXMLConfiguration: %v", err)
	}
	fmt.Println("Caches removed.")
}

func createSimpleCache(ctx context.Context, admin *hotrod.CacheAdmin) {
	err := admin.CreateCache(ctx, "SimpleCache", "")
	if err != nil {
		fmt.Printf("Expected to fail for multiple invocations as the cache exists: %v\n", err)
		return
	}
	fmt.Println("SimpleCache created.")
}

func createCacheWithXMLConfiguration(ctx context.Context, admin *hotrod.CacheAdmin) {
	if err := admin.GetOrCreateCache(ctx, "CacheWithXMLConfiguration", xmlConfig); err != nil {
		log.Fatalf("GetOrCreateCache: %v", err)
	}
	fmt.Println("Cache with XML configuration exists or is created.")
}
