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

	mm := client.Multimap("test")

	// Put multiple values under the same key (year -> names)
	if err := mm.Put(ctx, []byte("2016"), []byte("Rosita")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2016"), []byte("Guillermo")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2016"), []byte("Patricia")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2016"), []byte("Silvia")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2017"), []byte("Matilda")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2017"), []byte("Hector")); err != nil {
		log.Fatalf("Put: %v", err)
	}
	if err := mm.Put(ctx, []byte("2018"), []byte("Richard")); err != nil {
		log.Fatalf("Put: %v", err)
	}

	// Get all values for 2016
	values, err := mm.Get(ctx, []byte("2016"))
	if err != nil {
		log.Fatalf("Get: %v", err)
	}
	fmt.Println("People born in 2016:")
	for _, v := range values {
		fmt.Printf("  %s\n", v)
	}

	// Get the total multimap size
	size, err := mm.Size(ctx)
	if err != nil {
		log.Fatalf("Size: %v", err)
	}
	fmt.Printf("Total entries: %d\n", size)

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}
