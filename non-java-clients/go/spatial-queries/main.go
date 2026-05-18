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

const restaurantProto = `syntax = "proto3";
package tutorial;

/**
 * @Indexed
 * @GeoPoint(fieldName = "location", projectable = true, sortable = true)
 */
message Restaurant {
    /** @Keyword(normalizer = "lowercase", projectable = true, sortable = true) */
    string name = 1;
    /** @Text */
    string description = 2;
    /** @Text */
    string address = 3;
    /** @Latitude(fieldName = "location") */
    double latitude = 4;
    /** @Longitude(fieldName = "location") */
    double longitude = 5;
    /** @Basic */
    float score = 6;
}`

const trainRouteProto = `syntax = "proto3";
package tutorial;

/**
 * @Indexed
 * @GeoPoint(fieldName = "departure", projectable = true, sortable = true)
 * @GeoPoint(fieldName = "arrival", projectable = true, sortable = true)
 */
message TrainRoute {
    /** @Keyword(normalizer = "lowercase") */
    string name = 1;
    /** @Latitude(fieldName = "departure") */
    double departureLat = 2;
    /** @Longitude(fieldName = "departure") */
    double departureLon = 3;
    /** @Latitude(fieldName = "arrival") */
    double arrivalLat = 4;
    /** @Longitude(fieldName = "arrival") */
    double arrivalLon = 5;
}`

const indexedCacheConfig = `<distributed-cache>
    <encoding>
        <key media-type="application/x-protostream"/>
        <value media-type="application/x-protostream"/>
    </encoding>
    <indexing enabled="true" storage="local-heap">
        <indexed-entities>
            <indexed-entity>tutorial.Restaurant</indexed-entity>
            <indexed-entity>tutorial.TrainRoute</indexed-entity>
        </indexed-entities>
    </indexing>
</distributed-cache>`

type restaurant struct {
	name        string
	description string
	address     string
	latitude    float64
	longitude   float64
	score       float32
}

type trainRoute struct {
	name         string
	departureLat float64
	departureLon float64
	arrivalLat   float64
	arrivalLon   float64
}

const (
	myLat = 41.90847031512531
	myLon = 12.455633288333539
)

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

	// Register schemas
	if err := client.Schemas().Register(ctx, "restaurant.proto", restaurantProto); err != nil {
		log.Fatalf("Register restaurant schema: %v", err)
	}
	if err := client.Schemas().Register(ctx, "trainroute.proto", trainRouteProto); err != nil {
		log.Fatalf("Register trainroute schema: %v", err)
	}
	fmt.Println("Registered Restaurant and TrainRoute schemas.")

	if err := client.Administration().GetOrCreateCache(ctx, "spatialCache", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'spatialCache' ready.")

	cache := client.Cache("spatialCache")

	populateRestaurants(ctx, cache)
	populateTrainRoutes(ctx, cache)

	withinCircle(ctx, cache)
	withinBox(ctx, cache)
	withinPolygon(ctx, cache)
	distanceProjection(ctx, cache)
	orderByDistance(ctx, cache)
	trainRoutesDepartingNearBologna(ctx, cache)

	// Clean up
	if err := client.Administration().RemoveCache(ctx, "spatialCache"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}

var restaurants = []restaurant{
	{"La Locanda di Pietro", "Roman-style pasta dishes & Lazio region wines at a cozy traditional trattoria with a shaded terrace.", "Via Sebastiano Veniero, 28/c, 00192 Roma RM", 41.907903484609356, 12.45540543756422, 4.6},
	{"Scialla The Original Street Food", "Pastas & traditional pizza pies served in an unassuming eatery with vegetarian options.", "Vicolo del Farinone, 27, 00193 Roma RM", 41.90369455835456, 12.459566517195528, 4.7},
	{"Trattoria Pizzeria Gli Archi", "Traditional trattoria with exposed brick walls, serving up antipasti, pizzas & pasta dishes.", "Via Sebastiano Veniero, 26, 00192 Roma RM", 41.907930453801285, 12.455204785977637, 4.0},
	{"Alla Bracioleria Gracchi Restaurant", "", "Via dei Gracchi, 19, 00192 Roma RM", 41.907129402661795, 12.458927251586584, 4.7},
	{"Magazzino Scipioni", "Contemporary venue with a focus on unique wines & seasonal Italian plates, plus a bottle shop.", "Via degli Scipioni, 30, 00192 Roma RM", 41.90817843995448, 12.457118458698043, 4.6},
	{"Dal Toscano Restaurant", "Rich pastas, signature steaks & classic Tuscan dishes, plus Chianti wines, at a venerable trattoria.", "Via Germanico, 58-60, 00192 Roma RM", 41.90785274056548, 12.45822050287784, 4.2},
	{"Il Ciociaro", "Long-running, old-school restaurant plating traditional staples, from carbonara to tiramisu.", "Via Barletta, 21, 00192 Roma RM", 41.91038657525997, 12.458851939120656, 4.2},
}

var trainRoutes = []trainRoute{
	{"Rome-Milan", 41.8967, 12.4822, 45.4685, 9.1824},
	{"Bologna-Selva", 44.4949, 11.3426, 46.5560, 11.7559},
	{"Milan-Como", 45.4685, 9.1824, 45.8064, 9.0852},
	{"Bologna-Venice", 44.4949, 11.3426, 45.4404, 12.3160},
}

func populateRestaurants(ctx context.Context, cache *hotrod.RemoteCache) {
	for _, r := range restaurants {
		keyBytes := protostream.WrapString(r.name)
		valBytes := protostream.WrapMessage(encodeRestaurant(r), "tutorial.Restaurant")
		if err := cache.PutRaw(ctx, keyBytes, valBytes, codec.MediaIDProtostream); err != nil {
			log.Fatalf("Put restaurant %s: %v", r.name, err)
		}
	}
	fmt.Printf("Added %d restaurants in Rome.\n\n", len(restaurants))
}

func populateTrainRoutes(ctx context.Context, cache *hotrod.RemoteCache) {
	for _, r := range trainRoutes {
		keyBytes := protostream.WrapString(r.name)
		valBytes := protostream.WrapMessage(encodeTrainRoute(r), "tutorial.TrainRoute")
		if err := cache.PutRaw(ctx, keyBytes, valBytes, codec.MediaIDProtostream); err != nil {
			log.Fatalf("Put train route %s: %v", r.name, err)
		}
	}
	fmt.Printf("Added %d train routes.\n\n", len(trainRoutes))
}

func withinCircle(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Within circle (100m radius) ===")
	result := runQuery(ctx, cache,
		fmt.Sprintf("from tutorial.Restaurant r where r.location within circle(%f, %f, 100)", myLat, myLon))
	fmt.Printf("Found %d restaurants:\n", result.NumResults)
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		fmt.Printf("  %s\n", name)
	}
	fmt.Println()
}

func withinBox(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Within box ===")
	result := runQuery(ctx, cache,
		"from tutorial.Restaurant r where r.location within box(41.91, 12.45, 41.90, 12.46)")
	fmt.Printf("Found %d restaurants:\n", result.NumResults)
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		fmt.Printf("  %s\n", name)
	}
	fmt.Println()
}

func withinPolygon(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Within polygon ===")
	result := runQuery(ctx, cache,
		"from tutorial.Restaurant r where r.location within polygon((41.91, 12.45), (41.91, 12.46), (41.90, 12.46), (41.90, 12.45))")
	fmt.Printf("Found %d restaurants:\n", result.NumResults)
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		fmt.Printf("  %s\n", name)
	}
	fmt.Println()
}

func distanceProjection(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Distance projection ===")
	result := runQuery(ctx, cache,
		fmt.Sprintf("select r.name, distance(r.location, %f, %f) from tutorial.Restaurant r", myLat, myLon))
	for _, entry := range result.Entries {
		fmt.Printf("  %s: %.0fm\n", entry.Projections[0], toFloat64(entry.Projections[1]))
	}
	fmt.Println()
}

func orderByDistance(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Order by distance ===")
	result := runQuery(ctx, cache,
		fmt.Sprintf("from tutorial.Restaurant r order by distance(r.location, %f, %f)", myLat, myLon))
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		fmt.Printf("  %s\n", name)
	}
	fmt.Println()
}

func trainRoutesDepartingNearBologna(ctx context.Context, cache *hotrod.RemoteCache) {
	fmt.Println("=== Train routes departing near Bologna (300km) ===")
	result := runQuery(ctx, cache,
		"from tutorial.TrainRoute r where r.departure within circle(44.4949, 11.3426, 300000)")
	fmt.Printf("Found %d routes:\n", result.NumResults)
	for _, entry := range result.Entries {
		name, _ := readStringField(entry.Value, 1)
		fmt.Printf("  %s\n", name)
	}
	fmt.Println()
}

// --- Protobuf encoding ---

func encodeRestaurant(r restaurant) []byte {
	var dst []byte
	dst = appendLenDelimited(dst, 1, []byte(r.name))
	if r.description != "" {
		dst = appendLenDelimited(dst, 2, []byte(r.description))
	}
	dst = appendLenDelimited(dst, 3, []byte(r.address))
	dst = appendDoubleField(dst, 4, r.latitude)
	dst = appendDoubleField(dst, 5, r.longitude)
	dst = appendFloatField(dst, 6, r.score)
	return dst
}

func encodeTrainRoute(r trainRoute) []byte {
	var dst []byte
	dst = appendLenDelimited(dst, 1, []byte(r.name))
	dst = appendDoubleField(dst, 2, r.departureLat)
	dst = appendDoubleField(dst, 3, r.departureLon)
	dst = appendDoubleField(dst, 4, r.arrivalLat)
	dst = appendDoubleField(dst, 5, r.arrivalLon)
	return dst
}

func appendDoubleField(dst []byte, fieldNumber int, v float64) []byte {
	dst = appendTag(dst, fieldNumber, 1) // wireFixed64
	var buf [8]byte
	binary.LittleEndian.PutUint64(buf[:], math.Float64bits(v))
	return append(dst, buf[:]...)
}

func appendFloatField(dst []byte, fieldNumber int, v float32) []byte {
	dst = appendTag(dst, fieldNumber, 5) // wireFixed32
	var buf [4]byte
	binary.LittleEndian.PutUint32(buf[:], math.Float32bits(v))
	return append(dst, buf[:]...)
}

func appendTag(dst []byte, fieldNumber int, wireType int) []byte {
	return appendUvarint(dst, uint64(fieldNumber<<3|wireType))
}

func appendLenDelimited(dst []byte, fieldNumber int, data []byte) []byte {
	dst = appendTag(dst, fieldNumber, 2)
	dst = appendUvarint(dst, uint64(len(data)))
	return append(dst, data...)
}

func appendUvarint(dst []byte, v uint64) []byte {
	for v >= 0x80 {
		dst = append(dst, byte(v)|0x80)
		v >>= 7
	}
	return append(dst, byte(v))
}

// --- Query / decode helpers ---

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
