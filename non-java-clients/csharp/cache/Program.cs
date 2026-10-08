using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();
await admin.GetOrCreateCache("test");
Console.WriteLine("Cache 'test' ready.");

var cache = client.NewCache(new StringMarshaller(), new StringMarshaller(), "test");

await cache.Put("key", "value");

var result = await cache.Get("key");
Console.WriteLine($"key = {result}");

await admin.RemoveCache("test");
Console.WriteLine("Cache removed.");
