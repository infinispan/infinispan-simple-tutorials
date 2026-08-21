using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();

// Create a transactional cache
const string txCacheConfig = """
    <distributed-cache name="tx-cache">
        <locking isolation="REPEATABLE_READ"/>
        <transaction mode="NON_XA" locking="PESSIMISTIC"/>
    </distributed-cache>
    """;
await admin.GetOrCreateCache("tx-cache", txCacheConfig);

var cache = client.NewCache(new StringMarshaller(), new StringMarshaller(), "tx-cache");

// Transaction 1: commit
var tx = cache.BeginTransaction();
await tx.Put("key1", "value1");
await tx.Put("key2", "value2");
await tx.CommitAsync();

var v1 = await cache.Get("key1");
var v2 = await cache.Get("key2");
Console.WriteLine($"After commit: key1 = {v1}, key2 = {v2}");

// Transaction 2: rollback
tx = cache.BeginTransaction();
await tx.Put("key1", "value3");
await tx.Put("key2", "value4");
await tx.RollbackAsync();
Console.WriteLine("Transaction rolled back.");

v1 = await cache.Get("key1");
v2 = await cache.Get("key2");
Console.WriteLine($"After rollback: key1 = {v1}, key2 = {v2}");

// Cleanup
await admin.RemoveCache("tx-cache");
