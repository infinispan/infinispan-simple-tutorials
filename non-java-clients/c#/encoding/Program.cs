using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

const string textCacheConfig = """
    <distributed-cache name="textCache">
        <encoding media-type="text/plain"/>
    </distributed-cache>
    """;

const string jsonCacheConfig = """
    <distributed-cache name="jsonCache">
        <encoding media-type="application/json"/>
    </distributed-cache>
    """;

var admin = client.Administration();
await admin.GetOrCreateCache("textCache", textCacheConfig);
await admin.GetOrCreateCache("jsonCache", jsonCacheConfig);
Console.WriteLine("Caches ready.");

// Text cache with text/plain encoding
var textCache = client.NewCache<string>("textCache")
    .WithEncoding(MediaType.PlainText)
    .Build();

Console.WriteLine("\n== Cache with text/plain encoding ==");
await textCache.Put("greeting", "Hello, Infinispan!");
var textVal = await textCache.Get("greeting");
Console.WriteLine($"textCache[greeting] = {textVal}");

// JSON cache with application/json encoding
var jsonCache = client.NewCache<string>("jsonCache")
    .WithEncoding(MediaType.JSON)
    .Build();

Console.WriteLine("\n== Cache with application/json encoding ==");
await jsonCache.Put("\"name\"", "{\"project\": \"infinispan\"}");
var jsonVal = await jsonCache.Get("\"name\"");
Console.WriteLine($"jsonCache[name] = {jsonVal}");

// Clean up
await admin.RemoveCache("textCache");
await admin.RemoveCache("jsonCache");
Console.WriteLine("\nCaches removed.");
