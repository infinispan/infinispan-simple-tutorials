package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	"infinispan.org/go-client/internal/codec"
)

const textCacheConfig = `<distributed-cache name="textCache">
    <encoding media-type="text/plain"/>
</distributed-cache>`

const jsonCacheConfig = `<distributed-cache name="jsonCache">
    <encoding media-type="application/json"/>
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

	if err := admin.GetOrCreateCache(ctx, "textCache", textCacheConfig); err != nil {
		log.Fatalf("Create textCache: %v", err)
	}
	if err := admin.GetOrCreateCache(ctx, "jsonCache", jsonCacheConfig); err != nil {
		log.Fatalf("Create jsonCache: %v", err)
	}
	fmt.Println("Caches ready.")

	textCache := client.Cache("textCache")
	jsonCache := client.Cache("jsonCache")

	// Text cache with text/plain encoding
	fmt.Println("\n== Cache with text/plain encoding ==")
	if err := textCache.PutRaw(ctx, []byte("greeting"), []byte("Hello, Infinispan!"), codec.MediaIDTextPlain); err != nil {
		log.Fatalf("Put text: %v", err)
	}
	val, found, err := textCache.GetRaw(ctx, []byte("greeting"), codec.MediaIDTextPlain)
	if err != nil {
		log.Fatalf("Get text: %v", err)
	}
	if found {
		fmt.Printf("textCache[greeting] = %s\n", val)
	}

	// JSON cache with application/json encoding
	fmt.Println("\n== Cache with application/json encoding ==")
	if err := jsonCache.PutRaw(ctx, []byte(`"name"`), []byte(`{"project": "infinispan"}`), codec.MediaIDJSON); err != nil {
		log.Fatalf("Put json: %v", err)
	}
	val, found, err = jsonCache.GetRaw(ctx, []byte(`"name"`), codec.MediaIDJSON)
	if err != nil {
		log.Fatalf("Get json: %v", err)
	}
	if found {
		fmt.Printf("jsonCache[name] = %s\n", val)
	}

	// Clean up
	if err := admin.RemoveCache(ctx, "textCache"); err != nil {
		log.Fatalf("RemoveCache textCache: %v", err)
	}
	if err := admin.RemoveCache(ctx, "jsonCache"); err != nil {
		log.Fatalf("RemoveCache jsonCache: %v", err)
	}
	fmt.Println("\nCaches removed.")
}
