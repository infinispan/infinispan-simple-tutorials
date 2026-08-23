package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"infinispan.org/go-client/hotrod"
	pb "infinispan.org/tutorials/go/spatial-queries/proto"
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

	if err := client.Schemas().Register(ctx, "restaurant.proto", restaurantProto); err != nil {
		log.Fatalf("Register restaurant schema: %v", err)
	}
	if err := client.Schemas().Register(ctx, "trainroute.proto", trainRouteProto); err != nil {
		log.Fatalf("Register trainroute schema: %v", err)
	}
	fmt.Println("Registered Restaurant and TrainRoute schemas.")

	if err := client.Admin().GetOrCreateCache(ctx, "spatialCache", indexedCacheConfig); err != nil {
		log.Fatalf("Create cache: %v", err)
	}
	fmt.Println("Cache 'spatialCache' ready.")

	restaurantCache := hotrod.NewTypedCache[string, *pb.Restaurant](client, "spatialCache").Build()
	trainRouteCache := hotrod.NewTypedCache[string, *pb.TrainRoute](client, "spatialCache").Build()

	populateRestaurants(ctx, restaurantCache)
	populateTrainRoutes(ctx, trainRouteCache)

	withinCircle(ctx, restaurantCache)
	withinBox(ctx, restaurantCache)
	withinPolygon(ctx, restaurantCache)
	distanceProjection(ctx, restaurantCache)
	orderByDistance(ctx, restaurantCache)
	trainRoutesDepartingNearBologna(ctx, trainRouteCache)

	if err := client.Admin().RemoveCache(ctx, "spatialCache"); err != nil {
		log.Fatalf("RemoveCache: %v", err)
	}
	fmt.Println("Cache removed.")
}

var restaurants = []*pb.Restaurant{
	{Name: "La Locanda di Pietro", Description: "Roman-style pasta dishes & Lazio region wines at a cozy traditional trattoria with a shaded terrace.", Address: "Via Sebastiano Veniero, 28/c, 00192 Roma RM", Latitude: 41.907903484609356, Longitude: 12.45540543756422, Score: 4.6},
	{Name: "Scialla The Original Street Food", Description: "Pastas & traditional pizza pies served in an unassuming eatery with vegetarian options.", Address: "Vicolo del Farinone, 27, 00193 Roma RM", Latitude: 41.90369455835456, Longitude: 12.459566517195528, Score: 4.7},
	{Name: "Trattoria Pizzeria Gli Archi", Description: "Traditional trattoria with exposed brick walls, serving up antipasti, pizzas & pasta dishes.", Address: "Via Sebastiano Veniero, 26, 00192 Roma RM", Latitude: 41.907930453801285, Longitude: 12.455204785977637, Score: 4.0},
	{Name: "Alla Bracioleria Gracchi Restaurant", Address: "Via dei Gracchi, 19, 00192 Roma RM", Latitude: 41.907129402661795, Longitude: 12.458927251586584, Score: 4.7},
	{Name: "Magazzino Scipioni", Description: "Contemporary venue with a focus on unique wines & seasonal Italian plates, plus a bottle shop.", Address: "Via degli Scipioni, 30, 00192 Roma RM", Latitude: 41.90817843995448, Longitude: 12.457118458698043, Score: 4.6},
	{Name: "Dal Toscano Restaurant", Description: "Rich pastas, signature steaks & classic Tuscan dishes, plus Chianti wines, at a venerable trattoria.", Address: "Via Germanico, 58-60, 00192 Roma RM", Latitude: 41.90785274056548, Longitude: 12.45822050287784, Score: 4.2},
	{Name: "Il Ciociaro", Description: "Long-running, old-school restaurant plating traditional staples, from carbonara to tiramisu.", Address: "Via Barletta, 21, 00192 Roma RM", Latitude: 41.91038657525997, Longitude: 12.458851939120656, Score: 4.2},
}

var trainRoutes = []*pb.TrainRoute{
	{Name: "Rome-Milan", DepartureLat: 41.8967, DepartureLon: 12.4822, ArrivalLat: 45.4685, ArrivalLon: 9.1824},
	{Name: "Bologna-Selva", DepartureLat: 44.4949, DepartureLon: 11.3426, ArrivalLat: 46.5560, ArrivalLon: 11.7559},
	{Name: "Milan-Como", DepartureLat: 45.4685, DepartureLon: 9.1824, ArrivalLat: 45.8064, ArrivalLon: 9.0852},
	{Name: "Bologna-Venice", DepartureLat: 44.4949, DepartureLon: 11.3426, ArrivalLat: 45.4404, ArrivalLon: 12.3160},
}

func populateRestaurants(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	for _, r := range restaurants {
		if err := cache.Put(ctx, r.Name, r); err != nil {
			log.Fatalf("Put restaurant %s: %v", r.Name, err)
		}
	}
	fmt.Printf("Added %d restaurants in Rome.\n\n", len(restaurants))
}

func populateTrainRoutes(ctx context.Context, cache *hotrod.TypedCache[string, *pb.TrainRoute]) {
	for _, r := range trainRoutes {
		if err := cache.Put(ctx, r.Name, r); err != nil {
			log.Fatalf("Put train route %s: %v", r.Name, err)
		}
	}
	fmt.Printf("Added %d train routes.\n\n", len(trainRoutes))
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

func withinCircle(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	fmt.Println("=== Within circle (100m radius) ===")
	results, err := cache.Query(ctx,
		fmt.Sprintf("from tutorial.Restaurant r where r.location within circle(%f, %f, 100)", myLat, myLon))
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Found %d restaurants:\n", len(results))
	for _, r := range results {
		fmt.Printf("  %s\n", r.Name)
	}
	fmt.Println()
}

func withinBox(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	fmt.Println("=== Within box ===")
	results, err := cache.Query(ctx,
		"from tutorial.Restaurant r where r.location within box(41.91, 12.45, 41.90, 12.46)")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Found %d restaurants:\n", len(results))
	for _, r := range results {
		fmt.Printf("  %s\n", r.Name)
	}
	fmt.Println()
}

func withinPolygon(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	fmt.Println("=== Within polygon ===")
	results, err := cache.Query(ctx,
		"from tutorial.Restaurant r where r.location within polygon((41.91, 12.45), (41.91, 12.46), (41.90, 12.46), (41.90, 12.45))")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Found %d restaurants:\n", len(results))
	for _, r := range results {
		fmt.Printf("  %s\n", r.Name)
	}
	fmt.Println()
}

func distanceProjection(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	fmt.Println("=== Distance projection ===")
	result, err := cache.QueryProjection(ctx,
		fmt.Sprintf("select r.name, distance(r.location, %f, %f) from tutorial.Restaurant r", myLat, myLon))
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, entry := range result.Entries {
		fmt.Printf("  %s: %.0fm\n", entry.Projections[0], toFloat64(entry.Projections[1]))
	}
	fmt.Println()
}

func orderByDistance(ctx context.Context, cache *hotrod.TypedCache[string, *pb.Restaurant]) {
	fmt.Println("=== Order by distance ===")
	results, err := cache.Query(ctx,
		fmt.Sprintf("from tutorial.Restaurant r order by distance(r.location, %f, %f)", myLat, myLon))
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	for _, r := range results {
		fmt.Printf("  %s\n", r.Name)
	}
	fmt.Println()
}

func trainRoutesDepartingNearBologna(ctx context.Context, cache *hotrod.TypedCache[string, *pb.TrainRoute]) {
	fmt.Println("=== Train routes departing near Bologna (300km) ===")
	results, err := cache.Query(ctx,
		"from tutorial.TrainRoute r where r.departure within circle(44.4949, 11.3426, 300000)")
	if err != nil {
		log.Fatalf("Query: %v", err)
	}
	fmt.Printf("Found %d routes:\n", len(results))
	for _, r := range results {
		fmt.Printf("  %s\n", r.Name)
	}
	fmt.Println()
}
