package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const txCacheConfig = `<distributed-cache name="simple-tx-cache">
    <encoding media-type="application/x-protostream"/>
    <locking isolation="REPEATABLE_READ"/>
    <transaction locking="PESSIMISTIC" mode="NON_XA"/>
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

	if err := client.Administration().GetOrCreateCache(ctx, "simple-tx-cache", txCacheConfig); err != nil {
		log.Fatalf("GetOrCreateCache: %v", err)
	}

	cache := client.Cache("simple-tx-cache")

	// Transaction 1: commit — put key1 and key2, then commit
	err = client.WithTransaction(ctx, "simple-tx-cache", func(tc *hotrod.TxCache) error {
		if err := tc.Put(ctx, []byte("key1"), []byte("value1")); err != nil {
			return err
		}
		if err := tc.Put(ctx, []byte("key2"), []byte("value2")); err != nil {
			return err
		}
		return nil // returning nil commits the transaction
	})
	if err != nil {
		log.Fatalf("Transaction commit: %v", err)
	}

	// Verify committed values
	val1, _, _ := cache.Get(ctx, []byte("key1"))
	val2, _, _ := cache.Get(ctx, []byte("key2"))
	fmt.Printf("After commit:   key1 = %s, key2 = %s\n", val1, val2)

	// Transaction 2: rollback — modify key1 and key2, then rollback
	err = client.WithTransaction(ctx, "simple-tx-cache", func(tc *hotrod.TxCache) error {
		if err := tc.Put(ctx, []byte("key1"), []byte("value3")); err != nil {
			return err
		}
		if err := tc.Put(ctx, []byte("key2"), []byte("value4")); err != nil {
			return err
		}
		return errors.New("rollback") // returning an error rolls back the transaction
	})
	if err != nil {
		fmt.Printf("Transaction rolled back: %v\n", err)
	}

	// Verify values are unchanged after rollback
	val1, _, _ = cache.Get(ctx, []byte("key1"))
	val2, _, _ = cache.Get(ctx, []byte("key2"))
	fmt.Printf("After rollback: key1 = %s, key2 = %s\n", val1, val2)

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "simple-tx-cache"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}
