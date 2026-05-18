package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

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

	counters := client.Counters()

	// Define a strong counter with bounds
	fmt.Println("=== Strong Counter ===")
	created, err := counters.Define(ctx, "strong-counter", &hotrod.CounterConfiguration{
		Type:         hotrod.CounterStrong,
		Storage:      hotrod.StoragePersistent,
		Bounded:      true,
		LowerBound:   0,
		UpperBound:   100,
		InitialValue: 1,
	})
	if err != nil {
		log.Fatalf("Define strong counter: %v", err)
	}
	fmt.Printf("Strong counter created: %v\n", created)

	strong := counters.Counter("strong-counter")

	val, err := strong.Get(ctx)
	if err != nil {
		log.Fatalf("Get: %v", err)
	}
	fmt.Printf("Initial value: %d\n", val)

	// AddAndGet
	val, err = strong.AddAndGet(ctx, 10)
	if err != nil {
		log.Fatalf("AddAndGet: %v", err)
	}
	fmt.Printf("After adding 10: %d\n", val)

	// CompareAndSwap
	old, swapped, err := strong.CompareAndSwap(ctx, 11, 50)
	if err != nil {
		log.Fatalf("CompareAndSwap: %v", err)
	}
	fmt.Printf("CAS(11 -> 50): old=%d swapped=%v\n", old, swapped)

	val, _ = strong.Get(ctx)
	fmt.Printf("Current value: %d\n", val)

	// Reset
	if err := strong.Reset(ctx); err != nil {
		log.Fatalf("Reset: %v", err)
	}
	val, _ = strong.Get(ctx)
	fmt.Printf("After reset: %d\n", val)

	// Define a weak counter
	fmt.Println("\n=== Weak Counter ===")
	created, err = counters.Define(ctx, "weak-counter", &hotrod.CounterConfiguration{
		Type:    hotrod.CounterWeak,
		Storage: hotrod.StorageVolatile,
	})
	if err != nil {
		log.Fatalf("Define weak counter: %v", err)
	}
	fmt.Printf("Weak counter created: %v\n", created)

	weak := counters.Counter("weak-counter")

	// AddAndGet on weak counters returns 0 (fire-and-forget)
	_, err = weak.AddAndGet(ctx, 5)
	if err != nil {
		log.Fatalf("AddAndGet weak: %v", err)
	}
	val, _ = weak.Get(ctx)
	fmt.Printf("Weak counter value after adding 5: %d\n", val)

	// List all counter names
	names, err := counters.Names(ctx)
	if err != nil {
		log.Fatalf("Names: %v", err)
	}
	fmt.Printf("\nAll counters: %v\n", names)

	// Clean up
	if err := counters.Remove(ctx, "strong-counter"); err != nil {
		log.Fatalf("Remove strong-counter: %v", err)
	}
	if err := counters.Remove(ctx, "weak-counter"); err != nil {
		log.Fatalf("Remove weak-counter: %v", err)
	}
	fmt.Println("Counters removed.")
}
