using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();

// Create a simple cache with no extra configuration
await admin.CreateCache("SimpleCache");
Console.WriteLine("SimpleCache created.");

// Get or create a cache with an XML configuration
const string cacheConfig = """
    <distributed-cache name="withConfig">
        <locking isolation="REPEATABLE_READ"/>
        <transaction mode="NON_XA" locking="PESSIMISTIC"/>
        <expiration lifespan="60000" max-idle="1000"/>
    </distributed-cache>
    """;

await admin.GetOrCreateCache("withConfig", cacheConfig);
Console.WriteLine("Cache with XML configuration exists or is created.");

// List all cache names
var names = await admin.GetCacheNames();
Console.WriteLine($"Caches: {string.Join(", ", names)}");

// Remove both caches
await admin.RemoveCache("SimpleCache");
await admin.RemoveCache("withConfig");
Console.WriteLine("Caches removed.");
