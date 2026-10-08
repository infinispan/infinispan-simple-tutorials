using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();

// Create a normal cache
await admin.GetOrCreateCache("normal");

// Create a secured cache requiring the 'deployer' role
const string securedConfig = """
    <distributed-cache name="secured">
        <security>
            <authorization roles="deployer"/>
        </security>
    </distributed-cache>
    """;
await admin.GetOrCreateCache("secured", securedConfig);

var normalCache = client.NewCache(new StringMarshaller(), new StringMarshaller(), "normal");
var securedCache = client.NewCache(new StringMarshaller(), new StringMarshaller(), "secured");

// Put in normal cache - should succeed
try
{
    await normalCache.Put("key", "value");
    Console.WriteLine("Normal cache: put succeeded.");
}
catch (Exception ex)
{
    Console.WriteLine($"Normal cache put failed (unexpected): {ex.Message}");
}

// Put in secured cache - should fail (admin doesn't have the deployer role)
try
{
    await securedCache.Put("key", "value");
    Console.WriteLine("Secured cache: put succeeded (unexpected).");
}
catch (Exception ex)
{
    Console.WriteLine($"Secured cache: put failed as expected: {ex.Message}");
}

// Cleanup
await admin.RemoveCache("normal");
await admin.RemoveCache("secured");
