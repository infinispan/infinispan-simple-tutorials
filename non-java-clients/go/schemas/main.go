package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
)

const schemaName = "simple_tuto_hello.proto"

const validSchema = `syntax = "proto3";
package hello;

message Greeting {
    string content = 1;
}`

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

	schemas := client.Schemas()

	// Try to register an invalid schema
	fmt.Println("=== Registering schema with invalid content ===")
	err = schemas.Register(ctx, schemaName, "What is love?")
	if err != nil {
		fmt.Printf("Register with error content: %v\n", err)
	} else {
		fmt.Println("Schema registered (server may accept it with errors).")
	}

	// Register the corrected schema
	fmt.Println("\n=== Registering corrected schema ===")
	if err := schemas.Register(ctx, schemaName, validSchema); err != nil {
		log.Fatalf("Register valid schema: %v", err)
	}
	fmt.Println("Schema registered successfully.")

	// Retrieve the schema
	fmt.Printf("\n=== Getting schema '%s' ===\n", schemaName)
	content, found, err := schemas.Get(ctx, schemaName)
	if err != nil {
		log.Fatalf("Get schema: %v", err)
	}
	if found {
		fmt.Printf("Schema content:\n%s\n", content)
	} else {
		fmt.Println("Schema not found.")
	}
}
