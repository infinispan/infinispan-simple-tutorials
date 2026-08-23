package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	pb "infinispan.org/tutorials/go/vector-search/proto"
)

const beerProtoSchema = `syntax = "proto3";
package quickstart;

/* @Indexed */
message Beer {
  /* @Keyword(projectable = true, sortable = true) */
  string name = 1;
  /* @Keyword(projectable = true, normalizer = "lowercase") */
  string style = 2;
  /* @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
  string brewery = 3;
  /* @Keyword(projectable = true, normalizer = "lowercase") */
  string country = 4;
  /* @Basic(projectable = true, sortable = true) */
  double abv = 5;
  /* @Text */
  string description = 6;
  /* @Vector(dimension = 3, similarity = COSINE) */
  repeated float descriptionEmbedding = 7;
}`

const indexedCacheConfig = `<distributed-cache><encoding><key media-type="application/x-protostream"/><value media-type="application/x-protostream"/></encoding><indexing enabled="true" storage="local-heap"><indexed-entities><indexed-entity>quickstart.Beer</indexed-entity></indexed-entities></indexing></distributed-cache>`

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

	if err := client.Schemas().Register(ctx, "beer.proto", beerProtoSchema); err != nil {
		log.Fatalf("Register schema: %v", err)
	}
	fmt.Println("Registered beer.proto schema.")

	if err := client.Admin().GetOrCreateCache(ctx, "beers", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'beers' ready.")

	cache := hotrod.NewTypedCache[string, *pb.Beer](client, "beers").Build()

	populateBeers(ctx, cache)
	fmt.Printf("Loaded %d beers.\n\n", 11)

	fullTextSearch(ctx, cache)
	keywordAndRangeFilter(ctx, cache)
	projectionsAndSorting(ctx, cache)
	knnVectorSearch(ctx, cache)
	scoreProjection(ctx, cache)
	hybridFilterByStyle(ctx, cache)
	hybridFilterByCountry(ctx, cache)
	hybridFullTextAndVector(ctx, cache)
}

func populateBeers(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	beers := map[string]*pb.Beer{
		"beer:1": {Name: "Guinness", Style: "Stout", Brewery: "Guinness Brewery", Country: "Ireland", Abv: 4.2,
			Description:          "A rich, creamy stout with deep roasted barley flavours, hints of coffee and chocolate, and a velvety smooth finish.",
			DescriptionEmbedding: []float32{0.95, 0.05, 0.10}},
		"beer:2": {Name: "Delirium", Style: "Belgian Strong Ale", Brewery: "Brouwerij Huyghe", Country: "Belgium", Abv: 8.5,
			Description:          "A complex strong blonde ale with fruity esters, spicy phenols, and a warming alcohol presence balanced by a dry finish.",
			DescriptionEmbedding: []float32{0.30, 0.30, 0.70}},
		"beer:3": {Name: "Estrella Galicia", Style: "Lager", Brewery: "Hijos de Rivera", Country: "Spain", Abv: 5.5,
			Description:          "A crisp European lager with a balanced malt backbone, mild hop bitterness, and a clean refreshing finish.",
			DescriptionEmbedding: []float32{0.10, 0.90, 0.15}},
		"beer:4": {Name: "Mahou", Style: "Pilsner", Brewery: "Mahou San Miguel", Country: "Spain", Abv: 5.5,
			Description:          "A golden pilsner with delicate floral hop aromas, light biscuity malt, and a bright effervescent character.",
			DescriptionEmbedding: []float32{0.05, 0.85, 0.25}},
		"beer:5": {Name: "Corona Extra", Style: "Pale Lager", Brewery: "Grupo Modelo", Country: "Mexico", Abv: 4.5,
			Description:          "A light, easy-drinking pale lager with subtle sweetness, a hint of citrus, and a crisp dry finish best enjoyed ice-cold.",
			DescriptionEmbedding: []float32{0.05, 0.95, 0.10}},
		"beer:6": {Name: "Tactical Nuclear Penguin", Style: "Imperial Stout", Brewery: "BrewDog", Country: "Scotland", Abv: 32.0,
			Description:          "An extreme imperial stout aged in whisky casks, intensely smoky with dark chocolate, coffee, and dried fruit notes.",
			DescriptionEmbedding: []float32{0.98, 0.02, 0.20}},
		"beer:7": {Name: "Brahma", Style: "Lager", Brewery: "Ambev", Country: "Brazil", Abv: 4.3,
			Description:          "A light Brazilian lager, smooth and mildly sweet, brewed for easy drinking in warm weather.",
			DescriptionEmbedding: []float32{0.05, 0.92, 0.05}},
		"beer:8": {Name: "Radegast", Style: "Czech Lager", Brewery: "Radegast Brewery", Country: "Czech Republic", Abv: 5.0,
			Description:          "A traditional Czech lager with a prominent Saaz hop aroma, bready malt character, and a crisp bitter finish.",
			DescriptionEmbedding: []float32{0.15, 0.80, 0.30}},
		"beer:9": {Name: "Turia", Style: "Märzen", Brewery: "Turia Brewery", Country: "Spain", Abv: 5.4,
			Description:          "A toasted amber märzen from Valencia with caramel malt sweetness, a nutty aroma, and a smooth medium body.",
			DescriptionEmbedding: []float32{0.60, 0.40, 0.15}},
		"beer:10": {Name: "Hoptimus Prime", Style: "IPA", Brewery: "Hoptimus Brewing", Country: "USA", Abv: 7.5,
			Description:          "An aggressively hopped American IPA bursting with tropical fruit, pine resin, and grapefruit citrus over a sturdy malt backbone.",
			DescriptionEmbedding: []float32{0.10, 0.15, 0.95}},
		"beer:11": {Name: "Pagoa", Style: "Basque Ale", Brewery: "Pagoa Brewery", Country: "Spain", Abv: 5.0,
			Description:          "A craft ale from the Basque Country with earthy hops, a light fruity character, and a balanced malty sweetness.",
			DescriptionEmbedding: []float32{0.25, 0.35, 0.60}},
	}

	for key, b := range beers {
		if err := cache.Put(ctx, key, b); err != nil {
			log.Fatalf("Put %s: %v", key, err)
		}
	}
}

func toFloat64(v any) float64 {
	switch n := v.(type) {
	case float64:
		return n
	case float32:
		return float64(n)
	default:
		return 0
	}
}

// --- Queries ---

func fullTextSearch(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Full-text search: beers mentioning 'chocolate' ===")
	results, err := cache.Query(ctx, "from quickstart.Beer b where b.description : 'chocolate'")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, b := range results {
		fmt.Printf("  %-30s %s\n", b.Name, b.Style)
	}
	fmt.Println()
}

func keywordAndRangeFilter(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Keyword + range: Spanish beers under 5.5% ABV ===")
	results, err := cache.Query(ctx, "from quickstart.Beer b where b.country = 'Spain' and b.abv < 5.5")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, b := range results {
		fmt.Printf("  %-30s %.1f%%\n", b.Name, b.Abv)
	}
	fmt.Println()
}

func projectionsAndSorting(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Projections: all beers sorted by ABV ===")
	result, err := cache.QueryProjection(ctx, "select b.name, b.style, b.abv from quickstart.Beer b order by b.abv")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  %-30s %-20s %.1f%%\n", entry.Projections[0], entry.Projections[1], toFloat64(entry.Projections[2]))
	}
	fmt.Println()
}

func knnVectorSearch(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== kNN: 3 beers closest to 'dark roasty' vector [0.9, 0.1, 0.1] ===")
	results, err := cache.Query(ctx,
		"from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
		hotrod.WithQueryParam("v", []float32{0.9, 0.1, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, b := range results {
		fmt.Printf("  %-30s %s\n", b.Name, b.Style)
	}
	fmt.Println()
}

func scoreProjection(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== kNN with score: 3 beers closest to 'light crisp lager' vector [0.05, 0.9, 0.1] ===")
	result, err := cache.QueryProjection(ctx,
		"select b.name, b.style, score(b) from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
		hotrod.WithQueryParam("v", []float32{0.05, 0.9, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  %-30s %-20s score=%.4f\n", entry.Projections[0], entry.Projections[1], toFloat64(entry.Projections[2]))
	}
	fmt.Println()
}

func hybridFilterByStyle(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Hybrid: closest to 'refreshing summer beer' [0.05, 0.95, 0.05], only lagers under 5% ABV ===")
	result, err := cache.QueryProjection(ctx,
		"select score(b), b.name, b.style, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k "+
			"filtering (b.style = 'Lager' and b.abv < 5.0)",
		hotrod.WithQueryParam("v", []float32{0.05, 0.95, 0.05}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	if len(result.Entries) == 0 {
		fmt.Println("  (no matches)")
	}
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-10s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}

func hybridFilterByCountry(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Hybrid: closest to 'toasted malty caramel' [0.7, 0.3, 0.1], only Spanish beers ===")
	result, err := cache.QueryProjection(ctx,
		"select score(b), b.name, b.style, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k filtering b.country = 'Spain'",
		hotrod.WithQueryParam("v", []float32{0.7, 0.3, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-15s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}

func hybridFullTextAndVector(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Beer]) {
	fmt.Println("=== Hybrid: closest to 'hoppy craft' [0.1, 0.1, 0.95], description mentions 'citrus' ===")
	result, err := cache.QueryProjection(ctx,
		"select score(b), b.name, b.brewery, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k "+
			"filtering b.description : 'citrus'",
		hotrod.WithQueryParam("v", []float32{0.1, 0.1, 0.95}),
		hotrod.WithQueryParam("k", int32(5)),
	)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-20s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}
