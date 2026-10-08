using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();
await admin.GetOrCreateCache("people");

var mmap = client.NewMultimap(new StringMarshaller(), new StringMarshaller(), "people");

// People born in 2016
await mmap.Put("2016", "Oihane");
await mmap.Put("2016", "Elaia");
await mmap.Put("2016", "Iker");
await mmap.Put("2016", "Argia");

// People born in 2017
await mmap.Put("2017", "Jon");
await mmap.Put("2017", "June");

// People born in 2018
await mmap.Put("2018", "Zuri");

// Retrieve values for 2016
var values = await mmap.Get("2016");
Console.WriteLine("People born in 2016:");
foreach (var name in values)
    Console.WriteLine($"  {name}");

// Get total size
var size = await mmap.Size();
Console.WriteLine($"\nTotal people: {size}");

// Cleanup
await admin.RemoveCache("people");
