using Infinispan.Hotrod;
using Quickstart;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

const string beerProtoSchema = """
    syntax = "proto3";
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
                <indexed-entity>quickstart.Beer</indexed-entity>
            </indexed-entities>
        </indexing>
    </distributed-cache>
    """;

await client.Administration().Schemas().CreateOrUpdate("beer.proto", beerProtoSchema);
Console.WriteLine("Registered beer.proto schema.");

var admin = client.Administration();
await admin.GetOrCreateCache("beers", indexedCacheConfig);
Console.WriteLine("Cache 'beers' ready.");

var cache = client.NewCache<Beer>("beers")
    .WithEncoding(MediaType.Protobuf)
    .Build();

// Populate beers
var beers = new Dictionary<string, Beer>
{
    ["beer:1"] = new Beer { Name = "Guinness", Style = "Stout", Brewery = "Guinness Brewery", Country = "Ireland", Abv = 4.2,
        Description = "A rich, creamy stout with deep roasted barley flavours, hints of coffee and chocolate, and a velvety smooth finish.",
        DescriptionEmbedding = { 0.95f, 0.05f, 0.10f } },
    ["beer:2"] = new Beer { Name = "Delirium", Style = "Belgian Strong Ale", Brewery = "Brouwerij Huyghe", Country = "Belgium", Abv = 8.5,
        Description = "A complex strong blonde ale with fruity esters, spicy phenols, and a warming alcohol presence balanced by a dry finish.",
        DescriptionEmbedding = { 0.30f, 0.30f, 0.70f } },
    ["beer:3"] = new Beer { Name = "Estrella Galicia", Style = "Lager", Brewery = "Hijos de Rivera", Country = "Spain", Abv = 5.5,
        Description = "A crisp European lager with a balanced malt backbone, mild hop bitterness, and a clean refreshing finish.",
        DescriptionEmbedding = { 0.10f, 0.90f, 0.15f } },
    ["beer:4"] = new Beer { Name = "Mahou", Style = "Pilsner", Brewery = "Mahou San Miguel", Country = "Spain", Abv = 5.5,
        Description = "A golden pilsner with delicate floral hop aromas, light biscuity malt, and a bright effervescent character.",
        DescriptionEmbedding = { 0.05f, 0.85f, 0.25f } },
    ["beer:5"] = new Beer { Name = "Corona Extra", Style = "Pale Lager", Brewery = "Grupo Modelo", Country = "Mexico", Abv = 4.5,
        Description = "A light, easy-drinking pale lager with subtle sweetness, a hint of citrus, and a crisp dry finish best enjoyed ice-cold.",
        DescriptionEmbedding = { 0.05f, 0.95f, 0.10f } },
    ["beer:6"] = new Beer { Name = "Tactical Nuclear Penguin", Style = "Imperial Stout", Brewery = "BrewDog", Country = "Scotland", Abv = 32.0,
        Description = "An extreme imperial stout aged in whisky casks, intensely smoky with dark chocolate, coffee, and dried fruit notes.",
        DescriptionEmbedding = { 0.98f, 0.02f, 0.20f } },
    ["beer:7"] = new Beer { Name = "Brahma", Style = "Lager", Brewery = "Ambev", Country = "Brazil", Abv = 4.3,
        Description = "A light Brazilian lager, smooth and mildly sweet, brewed for easy drinking in warm weather.",
        DescriptionEmbedding = { 0.05f, 0.92f, 0.05f } },
    ["beer:8"] = new Beer { Name = "Radegast", Style = "Czech Lager", Brewery = "Radegast Brewery", Country = "Czech Republic", Abv = 5.0,
        Description = "A traditional Czech lager with a prominent Saaz hop aroma, bready malt character, and a crisp bitter finish.",
        DescriptionEmbedding = { 0.15f, 0.80f, 0.30f } },
    ["beer:9"] = new Beer { Name = "Turia", Style = "Märzen", Brewery = "Turia Brewery", Country = "Spain", Abv = 5.4,
        Description = "A toasted amber märzen from Valencia with caramel malt sweetness, a nutty aroma, and a smooth medium body.",
        DescriptionEmbedding = { 0.60f, 0.40f, 0.15f } },
    ["beer:10"] = new Beer { Name = "Hoptimus Prime", Style = "IPA", Brewery = "Hoptimus Brewing", Country = "USA", Abv = 7.5,
        Description = "An aggressively hopped American IPA bursting with tropical fruit, pine resin, and grapefruit citrus over a sturdy malt backbone.",
        DescriptionEmbedding = { 0.10f, 0.15f, 0.95f } },
    ["beer:11"] = new Beer { Name = "Pagoa", Style = "Basque Ale", Brewery = "Pagoa Brewery", Country = "Spain", Abv = 5.0,
        Description = "A craft ale from the Basque Country with earthy hops, a light fruity character, and a balanced malty sweetness.",
        DescriptionEmbedding = { 0.25f, 0.35f, 0.60f } },
};

await cache.PutAll(beers);
Console.WriteLine($"Loaded {beers.Count} beers.\n");

// Full-text search
Console.WriteLine("=== Full-text search: beers mentioning 'chocolate' ===");
var chocolateResults = await cache.Query<Beer>("from quickstart.Beer b where b.description : 'chocolate'");
foreach (var b in chocolateResults)
    Console.WriteLine($"  {b.Name,-30} {b.Style}");
Console.WriteLine();

// Keyword + range filter
Console.WriteLine("=== Keyword + range: Spanish beers under 5.5% ABV ===");
var spanishResults = await cache.Query<Beer>("from quickstart.Beer b where b.country = 'Spain' and b.abv < 5.5");
foreach (var b in spanishResults)
    Console.WriteLine($"  {b.Name,-30} {b.Abv:F1}%");
Console.WriteLine();

// Projections with sorting
Console.WriteLine("=== Projections: all beers sorted by ABV ===");
var sortedResults = await cache.Query("select b.name, b.style, b.abv from quickstart.Beer b order by b.abv");
foreach (object[] row in sortedResults)
    Console.WriteLine($"  {row[0],-30} {row[1],-20} {Convert.ToDouble(row[2]):F1}%");
Console.WriteLine();

// kNN vector search
Console.WriteLine("=== kNN: 3 beers closest to 'dark roasty' vector [0.9, 0.1, 0.1] ===");
var knnResults = await cache.Query<Beer>(
    "from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
    new Dictionary<string, object> { ["v"] = new[] { 0.9f, 0.1f, 0.1f }, ["k"] = 3 });
foreach (var b in knnResults)
    Console.WriteLine($"  {b.Name,-30} {b.Style}");
Console.WriteLine();

// kNN with score projection
Console.WriteLine("=== kNN with score: 3 beers closest to 'light crisp lager' vector [0.05, 0.9, 0.1] ===");
var scoreResults = await cache.Query(
    "select b.name, b.style, score(b) from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k",
    new Dictionary<string, object> { ["v"] = new[] { 0.05f, 0.9f, 0.1f }, ["k"] = 3 });
foreach (object[] row in scoreResults)
    Console.WriteLine($"  {row[0],-30} {row[1],-20} score={Convert.ToDouble(row[2]):F4}");
Console.WriteLine();

// Hybrid: vector + metadata filter by style
Console.WriteLine("=== Hybrid: closest to 'refreshing summer beer' [0.05, 0.95, 0.05], only lagers under 5% ABV ===");
var hybridStyleResults = await cache.Query(
    "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
    "where b.descriptionEmbedding <-> [:v]~:k " +
    "filtering (b.style = 'Lager' and b.abv < 5.0)",
    new Dictionary<string, object> { ["v"] = new[] { 0.05f, 0.95f, 0.05f }, ["k"] = 3 });
if (hybridStyleResults.Count == 0)
    Console.WriteLine("  (no matches)");
foreach (object[] row in hybridStyleResults)
    Console.WriteLine($"  score={Convert.ToDouble(row[0]):F4}  {row[1],-30} {row[2],-10} {Convert.ToDouble(row[3]):F1}%");
Console.WriteLine();

// Hybrid: vector + country filter
Console.WriteLine("=== Hybrid: closest to 'toasted malty caramel' [0.7, 0.3, 0.1], only Spanish beers ===");
var hybridCountryResults = await cache.Query(
    "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
    "where b.descriptionEmbedding <-> [:v]~:k filtering b.country = 'Spain'",
    new Dictionary<string, object> { ["v"] = new[] { 0.7f, 0.3f, 0.1f }, ["k"] = 3 });
foreach (object[] row in hybridCountryResults)
    Console.WriteLine($"  score={Convert.ToDouble(row[0]):F4}  {row[1],-30} {row[2],-15} {Convert.ToDouble(row[3]):F1}%");
Console.WriteLine();

// Hybrid: vector + full-text filter
Console.WriteLine("=== Hybrid: closest to 'hoppy craft' [0.1, 0.1, 0.95], description mentions 'citrus' ===");
var hybridTextResults = await cache.Query(
    "select score(b), b.name, b.brewery, b.abv from quickstart.Beer b " +
    "where b.descriptionEmbedding <-> [:v]~:k " +
    "filtering b.description : 'citrus'",
    new Dictionary<string, object> { ["v"] = new[] { 0.1f, 0.1f, 0.95f }, ["k"] = 5 });
foreach (object[] row in hybridTextResults)
    Console.WriteLine($"  score={Convert.ToDouble(row[0]):F4}  {row[1],-30} {row[2],-20} {Convert.ToDouble(row[3]):F1}%");
Console.WriteLine();

// Clean up
await admin.RemoveCache("beers");
Console.WriteLine("Cache removed.");
