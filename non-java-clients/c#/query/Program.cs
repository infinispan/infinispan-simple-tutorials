using Infinispan.Hotrod;
using Infinispan.Hotrod.Linq;
using Tutorial;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

// Schema registered on the server includes Infinispan indexing annotations
const string personProto = """
    syntax = "proto3";
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
    }
    """;

const string indexedCacheConfig = """
    <distributed-cache statistics="true">
        <encoding>
            <key media-type="application/x-protostream"/>
            <value media-type="application/x-protostream"/>
        </encoding>
        <indexing enabled="true" storage="local-heap">
            <indexed-entities>
                <indexed-entity>tutorial.Person</indexed-entity>
            </indexed-entities>
        </indexing>
    </distributed-cache>
    """;

// Register the Person proto schema
await client.Administration().Schemas().CreateOrUpdate("person.proto", personProto);
Console.WriteLine("Registered person.proto schema.");

// Create the indexed cache
var admin = client.Administration();
await admin.GetOrCreateCache("indexedPeopleCache", indexedCacheConfig);
Console.WriteLine("Cache 'indexedPeopleCache' ready.");

// Marshallers are inferred from the Person type and the Protobuf encoding — no manual setup needed
var cache = client.NewCache<Person>("indexedPeopleCache")
    .WithEncoding(MediaType.Protobuf)
    .Build();

// Add data
var people = new Dictionary<string, Person>
{
    ["hgranger"] = new Person { FirstName = "Hermione", LastName = "Granger", BornYear = 1990, BornIn = "London" },
    ["hpotter"] = new Person { FirstName = "Harry", LastName = "Potter", BornYear = 1991, BornIn = "Godric's Hollow" },
    ["rwesley"] = new Person { FirstName = "Ron", LastName = "Wesley", BornYear = 1990, BornIn = "London" },
    ["dmalfoy"] = new Person { FirstName = "Draco", LastName = "Malfoy", BornYear = 1989, BornIn = "London" },
};
await cache.PutAll(people);
Console.WriteLine($"Added {people.Count} people.\n");

// =====================================================================
// Ickle string queries
// =====================================================================
Console.WriteLine("╔══════════════════════════════════════╗");
Console.WriteLine("║     Ickle String Queries             ║");
Console.WriteLine("╚══════════════════════════════════════╝\n");

// Query all
Console.WriteLine("--- Query all ---");
var allResults = await cache.Query<Person>("from tutorial.Person");
Console.WriteLine($"Total results: {allResults.Count}");
foreach (var p in allResults)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Query with named parameter
Console.WriteLine("--- People with lastName = 'Granger' ---");
var grangerResults = await cache.Query<Person>("from tutorial.Person p where p.lastName = :lastName",
    new Dictionary<string, object> { ["lastName"] = "Granger" });
foreach (var p in grangerResults)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Query with projection
Console.WriteLine("--- Projection: people born in London ---");
var londonResults = await cache.Query("select p.firstName, p.lastName from tutorial.Person p where p.bornIn = 'London'");
foreach (object[] row in londonResults)
    Console.WriteLine($"  {row[0]} {row[1]}");
Console.WriteLine();

// =====================================================================
// LINQ queries
// =====================================================================
Console.WriteLine("╔══════════════════════════════════════╗");
Console.WriteLine("║     LINQ Queries                     ║");
Console.WriteLine("╚══════════════════════════════════════╝\n");

// Query all via LINQ
Console.WriteLine("--- Query all ---");
var allLinq = await cache.AsQueryable()
    .ToListAsync();
Console.WriteLine($"Total results: {allLinq.Count}");
foreach (var p in allLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Query with filter
Console.WriteLine("--- People with lastName = 'Granger' ---");
var grangerLinq = await cache.AsQueryable()
    .Where(p => p.LastName == "Granger")
    .ToListAsync();
foreach (var p in grangerLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Filter with captured variable
Console.WriteLine("--- People born in London (captured variable) ---");
string city = "London";
var londonLinq = await cache.AsQueryable()
    .Where(p => p.BornIn == city)
    .OrderBy(p => p.LastName)
    .ToListAsync();
foreach (var p in londonLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}, born {p.BornYear}");
Console.WriteLine();

// Combined filter with AND
Console.WriteLine("--- People born in London after 1989 ---");
var filteredLinq = await cache.AsQueryable()
    .Where(p => p.BornIn == "London" && p.BornYear > 1989)
    .ToListAsync();
foreach (var p in filteredLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Pagination
Console.WriteLine("--- All people, skip 1, take 2 ---");
var pagedLinq = await cache.AsQueryable()
    .OrderBy(p => p.LastName)
    .Skip(1)
    .Take(2)
    .ToListAsync();
foreach (var p in pagedLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Count
Console.WriteLine("--- Count people born in London ---");
var londonCount = await cache.AsQueryable()
    .Where(p => p.BornIn == "London")
    .CountAsync();
Console.WriteLine($"  Count: {londonCount}");
Console.WriteLine();

// FirstAsync
Console.WriteLine("--- First person named 'Harry' ---");
var harry = await cache.AsQueryable()
    .Where(p => p.FirstName == "Harry")
    .FirstAsync();
Console.WriteLine($"  {harry.FirstName} {harry.LastName}, born in {harry.BornIn}");
Console.WriteLine();

// String contains (LIKE)
Console.WriteLine("--- People whose first name contains 'ro' ---");
var containsLinq = await cache.AsQueryable()
    .Where(p => p.FirstName.Contains("ro"))
    .ToListAsync();
foreach (var p in containsLinq)
    Console.WriteLine($"  {p.FirstName} {p.LastName}");
Console.WriteLine();

// Clean up
await admin.RemoveCache("indexedPeopleCache");
Console.WriteLine("Cache removed.");
