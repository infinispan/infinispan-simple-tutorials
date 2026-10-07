using System.Diagnostics;
using Infinispan.Hotrod;

const int numEntries = 20;
const int numReads = 10_000;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();

// Create caches
await admin.GetOrCreateCache("benchmark-remote");
await admin.GetOrCreateCache("benchmark-near");

var remote = client.NewCache(new StringMarshaller(), new StringMarshaller(), "benchmark-remote");
var near = client.NewCache(new StringMarshaller(), new StringMarshaller(), "benchmark-near");
await near.EnableNearCache(numEntries);

// Populate both caches
for (var i = 0; i < numEntries; i++)
{
    await remote.Put($"key-{i}", $"value-{i}");
    await near.Put($"key-{i}", $"value-{i}");
}
Console.WriteLine($"Populated {numEntries} entries in both caches.");

// Prime the near cache with Gets
for (var i = 0; i < numEntries; i++)
    await near.Get($"key-{i}");

// Benchmark remote reads
var rng = new Random(42);
var sw = Stopwatch.StartNew();
for (var i = 0; i < numReads; i++)
    await remote.Get($"key-{rng.Next(numEntries)}");
sw.Stop();
var remoteTime = sw.Elapsed;
Console.WriteLine($"Remote cache: {numReads} reads in {remoteTime}");

// Benchmark near cache reads
rng = new Random(42);
sw.Restart();
for (var i = 0; i < numReads; i++)
    await near.Get($"key-{rng.Next(numEntries)}");
sw.Stop();
var nearTime = sw.Elapsed;
Console.WriteLine($"Near   cache: {numReads} reads in {nearTime}");

Console.WriteLine($"Near cache is {remoteTime / nearTime:F1}x faster.");

// Cleanup
await admin.RemoveCache("benchmark-remote");
await admin.RemoveCache("benchmark-near");
