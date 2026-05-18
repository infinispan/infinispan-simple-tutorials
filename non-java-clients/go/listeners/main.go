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

	cache := client.Cache("test")

	// Register a listener for created, modified, and removed events
	listener, err := cache.AddListener(ctx,
		hotrod.WithListenerInterests(hotrod.EventCreated, hotrod.EventModified, hotrod.EventRemoved),
	)
	if err != nil {
		log.Fatalf("AddListener: %v", err)
	}
	fmt.Println("Listener registered.")

	// Drain events in a goroutine
	done := make(chan struct{})
	go func() {
		defer close(done)
		for event := range listener.Events {
			switch event.Type {
			case hotrod.EventCreated:
				fmt.Printf("Created %s\n", event.Key)
			case hotrod.EventModified:
				fmt.Printf("Modified %s\n", event.Key)
			case hotrod.EventRemoved:
				fmt.Printf("Removed %s\n", event.Key)
			}
		}
	}()

	// Put some entries (creates)
	cache.Put(ctx, []byte("key1"), []byte("value1"))
	cache.Put(ctx, []byte("key2"), []byte("value2"))

	// Modify an entry
	cache.Put(ctx, []byte("key1"), []byte("newValue"))

	// Remote events are asynchronous, wait briefly
	time.Sleep(time.Second)

	// Remove the listener
	if err := cache.RemoveListener(ctx, listener); err != nil {
		log.Fatalf("RemoveListener: %v", err)
	}
	<-done
	fmt.Println("Listener removed.")

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "test"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
}
