package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	pb "infinispan.org/tutorials/go/query/proto"
)

const personProto = `syntax = "proto3";
package tutorial;

/* @Indexed */
message Person {
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string firstName = 1;
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string lastName = 2;
    /* @Basic(projectable = true, sortable = true) */
    int32 bornYear = 3;
    /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string bornIn = 4;
}`

const indexedCacheConfig = `<distributed-cache statistics="true">
    <encoding>
        <key media-type="application/x-protostream"/>
        <value media-type="application/x-protostream"/>
    </encoding>
    <indexing enabled="true" storage="local-heap">
        <indexed-entities>
            <indexed-entity>tutorial.Person</indexed-entity>
        </indexed-entities>
    </indexing>
</distributed-cache>`

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

	if err := client.Schemas().Register(ctx, "person.proto", personProto); err != nil {
		log.Fatalf("Register schema: %v", err)
	}
	fmt.Println("Registered person.proto schema.")

	if err := client.Admin().GetOrCreateCache(ctx, "indexedPeopleCache", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'indexedPeopleCache' ready.")

	cache := hotrod.NewTypedCache[string, *pb.Person](client, "indexedPeopleCache").Build()

	addData(ctx, cache)
	queryAll(ctx, cache)
	queryWithWhere(ctx, cache)
	queryWithProjection(ctx, cache)

	if err := client.Admin().RemoveCache(ctx, "indexedPeopleCache"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}

func addData(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Person]) {
	people := map[string]*pb.Person{
		"hgranger": {FirstName: "Hermione", LastName: "Granger", BornYear: 1990, BornIn: "London"},
		"hpotter":  {FirstName: "Harry", LastName: "Potter", BornYear: 1991, BornIn: "Godric's Hollow"},
		"rwesley":  {FirstName: "Ron", LastName: "Wesley", BornYear: 1990, BornIn: "London"},
		"dmalfoy":  {FirstName: "Draco", LastName: "Malfoy", BornYear: 1989, BornIn: "London"},
	}
	for key, p := range people {
		if err := cache.Put(ctx, key, p); err != nil {
			log.Fatalf("Put %s: %v", key, err)
		}
	}
	fmt.Printf("Added %d people.\n\n", len(people))
}

func queryAll(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Person]) {
	fmt.Println("=== Query all ===")
	results, err := cache.Query(ctx, "from tutorial.Person")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Total results: %d\n", len(results))
	for _, p := range results {
		fmt.Printf("  %s %s\n", p.FirstName, p.LastName)
	}
	fmt.Println()
}

func queryWithWhere(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Person]) {
	fmt.Println("=== Query: people with lastName = 'Granger' ===")
	results, err := cache.Query(ctx, "from tutorial.Person p where p.lastName = :lastName",
		hotrod.WithQueryParam("lastName", "Granger"),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, p := range results {
		fmt.Printf("  %s %s\n", p.FirstName, p.LastName)
	}
	fmt.Println()
}

func queryWithProjection(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Person]) {
	fmt.Println("=== Query: projection of people born in London ===")
	result, err := cache.QueryProjection(ctx, "select p.firstName, p.lastName from tutorial.Person p where p.bornIn = 'London'")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  %s %s\n", entry.Projections[0], entry.Projections[1])
	}
	fmt.Println()
}
