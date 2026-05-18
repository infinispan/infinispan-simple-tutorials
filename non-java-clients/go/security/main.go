package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const testCacheConfig = `<distributed-cache name="test">
    <encoding media-type="application/x-protostream"/>
</distributed-cache>`

const securedCacheConfig = `<distributed-cache name="securedCache">
    <encoding media-type="application/x-protostream"/>
    <security>
        <authorization enabled="true" roles="deployer"/>
    </security>
</distributed-cache>`

func main() {
	uri := os.Getenv("INFINISPAN_URI")
	if uri == "" {
		uri = "hotrod://admin:password@localhost:11222"
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	// Connect with admin credentials (admin role, not deployer role)
	client, err := hotrod.NewClient(ctx, uri)
	if err != nil {
		log.Fatalf("NewClient: %v", err)
	}
	defer client.Close()

	admin := client.Administration()

	if err := admin.GetOrCreateCache(ctx, "test", testCacheConfig); err != nil {
		log.Fatalf("Create test cache: %v", err)
	}
	if err := admin.GetOrCreateCache(ctx, "securedCache", securedCacheConfig); err != nil {
		log.Fatalf("Create secured cache: %v", err)
	}

	// Store a value in a non-secured cache — should succeed
	cache := client.Cache("test")
	if err := cache.Put(ctx, []byte("key"), []byte("value")); err != nil {
		log.Fatalf("Put to test cache: %v", err)
	}
	fmt.Println("Successfully put value in 'test' cache.")

	// Store a value in a cache where the admin role does not have access — should fail
	securedCache := client.Cache("securedCache")
	err = securedCache.Put(ctx, []byte("key"), []byte("value"))
	if err != nil {
		fmt.Printf("Put to secured cache failed as expected: %v\n", err)
	} else {
		fmt.Println("Put to secured cache unexpectedly succeeded.")
	}

	// Clean up
	if err := admin.RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache test: %v", err)
	}
	if err := admin.RemoveCache(ctx, "securedCache"); err != nil {
		log.Fatalf("RemoveCache securedCache: %v", err)
	}
	fmt.Println("Caches removed.")
}
