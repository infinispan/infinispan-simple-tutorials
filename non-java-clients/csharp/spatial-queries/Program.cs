using Infinispan.Hotrod;
using Tutorial;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

const string restaurantProtoSchema = """
    syntax = "proto3";
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
    }
    """;

const string trainRouteProtoSchema = """
    syntax = "proto3";
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
    }
    """;

const string indexedCacheConfig = """
    <distributed-cache>
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
    </distributed-cache>
    """;

await client.Administration().Schemas().CreateOrUpdate("restaurant.proto", restaurantProtoSchema);
await client.Administration().Schemas().CreateOrUpdate("trainroute.proto", trainRouteProtoSchema);
Console.WriteLine("Registered Restaurant and TrainRoute schemas.");

var admin = client.Administration();
await admin.GetOrCreateCache("spatialCache", indexedCacheConfig);
Console.WriteLine("Cache 'spatialCache' ready.");

var restaurantCache = client.NewCache<Restaurant>("spatialCache")
    .WithEncoding(MediaType.Protobuf)
    .Build();

var trainRouteCache = client.NewCache<TrainRoute>("spatialCache")
    .WithEncoding(MediaType.Protobuf)
    .Build();

// Populate restaurants
var restaurants = new Dictionary<string, Restaurant>
{
    ["La Locanda di Pietro"] = new Restaurant { Name = "La Locanda di Pietro", Description = "Roman-style pasta dishes & Lazio region wines at a cozy traditional trattoria with a shaded terrace.", Address = "Via Sebastiano Veniero, 28/c, 00192 Roma RM", Latitude = 41.907903484609356, Longitude = 12.45540543756422, Score = 4.6f },
    ["Scialla The Original Street Food"] = new Restaurant { Name = "Scialla The Original Street Food", Description = "Pastas & traditional pizza pies served in an unassuming eatery with vegetarian options.", Address = "Vicolo del Farinone, 27, 00193 Roma RM", Latitude = 41.90369455835456, Longitude = 12.459566517195528, Score = 4.7f },
    ["Trattoria Pizzeria Gli Archi"] = new Restaurant { Name = "Trattoria Pizzeria Gli Archi", Description = "Traditional trattoria with exposed brick walls, serving up antipasti, pizzas & pasta dishes.", Address = "Via Sebastiano Veniero, 26, 00192 Roma RM", Latitude = 41.907930453801285, Longitude = 12.455204785977637, Score = 4.0f },
    ["Alla Bracioleria Gracchi Restaurant"] = new Restaurant { Name = "Alla Bracioleria Gracchi Restaurant", Address = "Via dei Gracchi, 19, 00192 Roma RM", Latitude = 41.907129402661795, Longitude = 12.458927251586584, Score = 4.7f },
    ["Magazzino Scipioni"] = new Restaurant { Name = "Magazzino Scipioni", Description = "Contemporary venue with a focus on unique wines & seasonal Italian plates, plus a bottle shop.", Address = "Via degli Scipioni, 30, 00192 Roma RM", Latitude = 41.90817843995448, Longitude = 12.457118458698043, Score = 4.6f },
    ["Dal Toscano Restaurant"] = new Restaurant { Name = "Dal Toscano Restaurant", Description = "Rich pastas, signature steaks & classic Tuscan dishes, plus Chianti wines, at a venerable trattoria.", Address = "Via Germanico, 58-60, 00192 Roma RM", Latitude = 41.90785274056548, Longitude = 12.45822050287784, Score = 4.2f },
    ["Il Ciociaro"] = new Restaurant { Name = "Il Ciociaro", Description = "Long-running, old-school restaurant plating traditional staples, from carbonara to tiramisu.", Address = "Via Barletta, 21, 00192 Roma RM", Latitude = 41.91038657525997, Longitude = 12.458851939120656, Score = 4.2f },
};

await restaurantCache.PutAll(restaurants);
Console.WriteLine($"Added {restaurants.Count} restaurants in Rome.\n");

// Populate train routes
var trainRoutes = new Dictionary<string, TrainRoute>
{
    ["Rome-Milan"] = new TrainRoute { Name = "Rome-Milan", DepartureLat = 41.8967, DepartureLon = 12.4822, ArrivalLat = 45.4685, ArrivalLon = 9.1824 },
    ["Bologna-Selva"] = new TrainRoute { Name = "Bologna-Selva", DepartureLat = 44.4949, DepartureLon = 11.3426, ArrivalLat = 46.5560, ArrivalLon = 11.7559 },
    ["Milan-Como"] = new TrainRoute { Name = "Milan-Como", DepartureLat = 45.4685, DepartureLon = 9.1824, ArrivalLat = 45.8064, ArrivalLon = 9.0852 },
    ["Bologna-Venice"] = new TrainRoute { Name = "Bologna-Venice", DepartureLat = 44.4949, DepartureLon = 11.3426, ArrivalLat = 45.4404, ArrivalLon = 12.3160 },
};

await trainRouteCache.PutAll(trainRoutes);
Console.WriteLine($"Added {trainRoutes.Count} train routes.\n");

const double myLat = 41.90847031512531;
const double myLon = 12.455633288333539;

// Within circle (100m radius)
Console.WriteLine("=== Within circle (100m radius) ===");
var circleResults = await restaurantCache.Query<Restaurant>(
    $"from tutorial.Restaurant r where r.location within circle({myLat}, {myLon}, 100)");
Console.WriteLine($"Found {circleResults.Count} restaurants:");
foreach (var r in circleResults)
    Console.WriteLine($"  {r.Name}");
Console.WriteLine();

// Within box
Console.WriteLine("=== Within box ===");
var boxResults = await restaurantCache.Query<Restaurant>(
    "from tutorial.Restaurant r where r.location within box(41.91, 12.45, 41.90, 12.46)");
Console.WriteLine($"Found {boxResults.Count} restaurants:");
foreach (var r in boxResults)
    Console.WriteLine($"  {r.Name}");
Console.WriteLine();

// Within polygon
Console.WriteLine("=== Within polygon ===");
var polygonResults = await restaurantCache.Query<Restaurant>(
    "from tutorial.Restaurant r where r.location within polygon((41.91, 12.45), (41.91, 12.46), (41.90, 12.46), (41.90, 12.45))");
Console.WriteLine($"Found {polygonResults.Count} restaurants:");
foreach (var r in polygonResults)
    Console.WriteLine($"  {r.Name}");
Console.WriteLine();

// Distance projection
Console.WriteLine("=== Distance projection ===");
var distResults = await restaurantCache.Query(
    $"select r.name, distance(r.location, {myLat}, {myLon}) from tutorial.Restaurant r");
foreach (object[] row in distResults)
    Console.WriteLine($"  {row[0]}: {Convert.ToDouble(row[1]):F0}m");
Console.WriteLine();

// Order by distance
Console.WriteLine("=== Order by distance ===");
var orderedResults = await restaurantCache.Query<Restaurant>(
    $"from tutorial.Restaurant r order by distance(r.location, {myLat}, {myLon})");
foreach (var r in orderedResults)
    Console.WriteLine($"  {r.Name}");
Console.WriteLine();

// Train routes departing near Bologna (300km)
Console.WriteLine("=== Train routes departing near Bologna (300km) ===");
var trainResults = await trainRouteCache.Query<TrainRoute>(
    "from tutorial.TrainRoute r where r.departure within circle(44.4949, 11.3426, 300000)");
Console.WriteLine($"Found {trainResults.Count} routes:");
foreach (var r in trainResults)
    Console.WriteLine($"  {r.Name}");
Console.WriteLine();

// Clean up
await admin.RemoveCache("spatialCache");
Console.WriteLine("Cache removed.");
