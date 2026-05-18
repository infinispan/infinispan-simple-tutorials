package main

import (
	"context"
	"encoding/binary"
	"fmt"
	"log"
	"math"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	"infinispan.org/go-client/internal/codec"
	"infinispan.org/go-client/internal/protostream"
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

type beer struct {
	name                 string
	style                string
	brewery              string
	country              string
	abv                  float64
	description          string
	descriptionEmbedding []float32
}

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

	if err := client.Administration().GetOrCreateCache(ctx, "beers", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'beers' ready.")

	cache := client.Cache("beers")
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

func encodeBeer(b beer) []byte {
	var dst []byte
	dst = appendLenDelimited(dst, 1, []byte(b.name))
	dst = appendLenDelimited(dst, 2, []byte(b.style))
	dst = appendLenDelimited(dst, 3, []byte(b.brewery))
	dst = appendLenDelimited(dst, 4, []byte(b.country))

	// field 5: double (wire type fixed64)
	dst = appendTag(dst, 5, 1) // wireFixed64
	var dbuf [8]byte
	binary.LittleEndian.PutUint64(dbuf[:], math.Float64bits(b.abv))
	dst = append(dst, dbuf[:]...)

	dst = appendLenDelimited(dst, 6, []byte(b.description))

	// field 7: repeated float, packed encoding
	if len(b.descriptionEmbedding) > 0 {
		packed := make([]byte, 0, 4*len(b.descriptionEmbedding))
		for _, f := range b.descriptionEmbedding {
			var fbuf [4]byte
			binary.LittleEndian.PutUint32(fbuf[:], math.Float32bits(f))
			packed = append(packed, fbuf[:]...)
		}
		dst = appendLenDelimited(dst, 7, packed)
	}

	return dst
}

// Minimal protobuf encoding helpers (same logic as protostream package).
func appendTag(dst []byte, fieldNumber int, wireType int) []byte {
	return appendUvarint(dst, uint64(fieldNumber<<3|wireType))
}

func appendLenDelimited(dst []byte, fieldNumber int, data []byte) []byte {
	dst = appendTag(dst, fieldNumber, 2) // wireLengthDelimited
	dst = appendUvarint(dst, uint64(len(data)))
	dst = append(dst, data...)
	return dst
}

func appendUvarint(dst []byte, v uint64) []byte {
	for v >= 0x80 {
		dst = append(dst, byte(v)|0x80)
		v >>= 7
	}
	dst = append(dst, byte(v))
	return dst
}

func putBeer(ctx context.Context, cache *hotrod.RemoteCache, key string, b beer) {
	keyBytes := protostream.WrapString(key)
	valueBytes := protostream.WrapMessage(encodeBeer(b), "quickstart.Beer")
	if err := cache.PutRaw(ctx, keyBytes, valueBytes, codec.MediaIDProtostream); err != nil {
		log.Fatalf("Put %s: %v", key, err)
	}
}

func populateBeers(ctx context.Context, cache *hotrod.RemoteCache) {
	beers := map[string]beer{
		"beer:1": {"Guinness", "Stout", "Guinness Brewery", "Ireland", 4.2,
			"A rich, creamy stout with deep roasted barley flavours, hints of coffee and chocolate, and a velvety smooth finish.",
			[]float32{0.95, 0.05, 0.10}},
		"beer:2": {"Delirium", "Belgian Strong Ale", "Brouwerij Huyghe", "Belgium", 8.5,
			"A complex strong blonde ale with fruity esters, spicy phenols, and a warming alcohol presence balanced by a dry finish.",
			[]float32{0.30, 0.30, 0.70}},
		"beer:3": {"Estrella Galicia", "Lager", "Hijos de Rivera", "Spain", 5.5,
			"A crisp European lager with a balanced malt backbone, mild hop bitterness, and a clean refreshing finish.",
			[]float32{0.10, 0.90, 0.15}},
		"beer:4": {"Mahou", "Pilsner", "Mahou San Miguel", "Spain", 5.5,
			"A golden pilsner with delicate floral hop aromas, light biscuity malt, and a bright effervescent character.",
			[]float32{0.05, 0.85, 0.25}},
		"beer:5": {"Corona Extra", "Pale Lager", "Grupo Modelo", "Mexico", 4.5,
			"A light, easy-drinking pale lager with subtle sweetness, a hint of citrus, and a crisp dry finish best enjoyed ice-cold.",
			[]float32{0.05, 0.95, 0.10}},
		"beer:6": {"Tactical Nuclear Penguin", "Imperial Stout", "BrewDog", "Scotland", 32.0,
			"An extreme imperial stout aged in whisky casks, intensely smoky with dark chocolate, coffee, and dried fruit notes.",
			[]float32{0.98, 0.02, 0.20}},
		"beer:7": {"Brahma", "Lager", "Ambev", "Brazil", 4.3,
			"A light Brazilian lager, smooth and mildly sweet, brewed for easy drinking in warm weather.",
			[]float32{0.05, 0.92, 0.05}},
		"beer:8": {"Radegast", "Czech Lager", "Radegast Brewery", "Czech Republic", 5.0,
			"A traditional Czech lager with a prominent Saaz hop aroma, bready malt character, and a crisp bitter finish.",
			[]float32{0.15, 0.80, 0.30}},
		"beer:9": {"Turia", "Märzen", "Turia Brewery", "Spain", 5.4,
			"A toasted amber märzen from Valencia with caramel malt sweetness, a nutty aroma, and a smooth medium body.",
			[]float32{0.60, 0.40, 0.15}},
		"beer:10": {"Hoptimus Prime", "IPA", "Hoptimus Brewing", "USA", 7.5,
			"An aggressively hopped American IPA bursting with tropical fruit, pine resin, and grapefruit citrus over a sturdy malt backbone.",
			[]float32{0.10, 0.15, 0.95}},
		"beer:11": {"Pagoa", "Basque Ale", "Pagoa Brewery", "Spain", 5.0,
			"A craft ale from the Basque Country with earthy hops, a light fruity character, and a balanced malty sweetness.",
			[]float32{0.25, 0.35, 0.60}},
	}

	for key, b := range beers {
		putBeer(ctx, cache, key, b)
	}
}

// --- Query helpers ---

func runQuery(ctx context.Context, cache *hotrod.RemoteCache, ickle string, opts ...hotrod.QueryOption) *hotrod.QueryResult {
	result, err := cache.Query(ctx, ickle, opts...)
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	return result
}

func readStringField(data []byte, targetField int) (string, bool) {
	var result string
	var found bool
	scanFields(data, func(fn, wt int, val []byte) {
		if fn == targetField && wt == 2 {
			result = string(val)
			found = true
		}
	})
	return result, found
}

func readDoubleField(data []byte, targetField int) (float64, bool) {
	var result float64
	var found bool
	scanFields(data, func(fn, wt int, val []byte) {
		if fn == targetField && wt == 1 && len(val) == 8 {
			result = math.Float64frombits(binary.LittleEndian.Uint64(val))
			found = true
		}
	})
	return result, found
}

func scanFields(data []byte, fn func(fieldNumber, wireType int, value []byte)) {
	pos := 0
	for pos < len(data) {
		tag, n := decodeUvarint(data[pos:])
		if n <= 0 {
			return
		}
		pos += n
		fieldNumber := int(tag >> 3)
		wireType := int(tag & 0x7)

		switch wireType {
		case 0: // varint
			start := pos
			for pos < len(data) && data[pos] >= 0x80 {
				pos++
			}
			if pos < len(data) {
				pos++
			}
			fn(fieldNumber, wireType, data[start:pos])
		case 1: // fixed64
			if pos+8 <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+8])
				pos += 8
			}
		case 2: // length-delimited
			length, n := decodeUvarint(data[pos:])
			if n <= 0 {
				return
			}
			pos += n
			if pos+int(length) <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+int(length)])
				pos += int(length)
			}
		case 5: // fixed32
			if pos+4 <= len(data) {
				fn(fieldNumber, wireType, data[pos:pos+4])
				pos += 4
			}
		default:
			return
		}
	}
}

func decodeUvarint(data []byte) (uint64, int) {
	var v uint64
	for i, b := range data {
		if i >= 10 {
			return 0, -1
		}
		v |= uint64(b&0x7F) << (7 * i)
		if b < 0x80 {
			return v, i + 1
		}
	}
	return 0, -1
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

func fullTextSearch(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Full-text search: beers mentioning 'chocolate' ===")
	result := runQuery(ctx, cache, "from quickstart.Beer b where b.description : 'chocolate'")
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		style, _ := readStringField(entry.Value, 2)
		fmt.Printf("  %-30s %s\n", name, style)
	}
	fmt.Println()
}

func keywordAndRangeFilter(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Keyword + range: Spanish beers under 5.5% ABV ===")
	result := runQuery(ctx, cache, "from quickstart.Beer b where b.country = 'Spain' and b.abv < 5.5")
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		abv, _ := readDoubleField(entry.Value, 5)
		fmt.Printf("  %-30s %.1f%%\n", name, abv)
	}
	fmt.Println()
}

func projectionsAndSorting(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Projections: all beers sorted by ABV ===")
	result := runQuery(ctx, cache, "select b.name, b.style, b.abv from quickstart.Beer b order by b.abv")
	for _, entry := range result.Entries {
		fmt.Printf("  %-30s %-20s %.1f%%\n", entry.Projections[0], entry.Projections[1], toFloat64(entry.Projections[2]))
	}
	fmt.Println()
}

func knnVectorSearch(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== kNN: 3 beers closest to 'dark roasty' vector [0.9, 0.1, 0.1] ===")
	result := runQuery(ctx, cache,
		"from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
		hotrod.WithQueryParam("v", []float32{0.9, 0.1, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		style, _ := readStringField(entry.Value, 2)
		fmt.Printf("  %-30s %s\n", name, style)
	}
	fmt.Println()
}

func scoreProjection(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== kNN with score: 3 beers closest to 'light crisp lager' vector [0.05, 0.9, 0.1] ===")
	result := runQuery(ctx, cache,
		"select b.name, b.style, score(b) from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
		hotrod.WithQueryParam("v", []float32{0.05, 0.9, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	for _, entry := range result.Entries {
		fmt.Printf("  %-30s %-20s score=%.4f\n", entry.Projections[0], entry.Projections[1], toFloat64(entry.Projections[2]))
	}
	fmt.Println()
}

func hybridFilterByStyle(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Hybrid: closest to 'refreshing summer beer' [0.05, 0.95, 0.05], only lagers under 5% ABV ===")
	result := runQuery(ctx, cache,
		"select score(b), b.name, b.style, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k "+
			"filtering (b.style = 'Lager' and b.abv < 5.0)",
		hotrod.WithQueryParam("v", []float32{0.05, 0.95, 0.05}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	if len(result.Entries) == 0 {
		fmt.Println("  (no matches)")
	}
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-10s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}

func hybridFilterByCountry(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Hybrid: closest to 'toasted malty caramel' [0.7, 0.3, 0.1], only Spanish beers ===")
	result := runQuery(ctx, cache,
		"select score(b), b.name, b.style, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k filtering b.country = 'Spain'",
		hotrod.WithQueryParam("v", []float32{0.7, 0.3, 0.1}),
		hotrod.WithQueryParam("k", int32(3)),
	)
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-15s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}

func hybridFullTextAndVector(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Hybrid: closest to 'hoppy craft' [0.1, 0.1, 0.95], description mentions 'citrus' ===")
	result := runQuery(ctx, cache,
		"select score(b), b.name, b.brewery, b.abv from quickstart.Beer b "+
			"where b.descriptionEmbedding <-> [:v]~:k "+
			"filtering b.description : 'citrus'",
		hotrod.WithQueryParam("v", []float32{0.1, 0.1, 0.95}),
		hotrod.WithQueryParam("k", int32(5)),
	)
	for _, entry := range result.Entries {
		fmt.Printf("  score=%.4f  %-30s %-20s %.1f%%\n",
			toFloat64(entry.Projections[0]), entry.Projections[1], entry.Projections[2], toFloat64(entry.Projections[3]))
	}
	fmt.Println()
}
