using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var schemas = client.Administration().Schemas();

// Try to register an invalid schema
const string invalidSchema = "not a valid .proto file";
try
{
    await schemas.CreateOrUpdate("invalid.proto", invalidSchema);
    Console.WriteLine("Invalid schema accepted (unexpected).");
}
catch (Exception ex)
{
    Console.WriteLine($"Invalid schema rejected: {ex.Message}");
}

// Register a valid schema
const string validSchema = """
    syntax = "proto2";

    message Greeting {
        required string name = 1;
        optional string text = 2;
    }
    """;
await schemas.CreateOrUpdate("greeting.proto", validSchema);
Console.WriteLine("Schema 'greeting.proto' registered.");

// Cleanup
await schemas.Delete("greeting.proto");
await schemas.Delete("invalid.proto");
Console.WriteLine("Schemas deleted.");
