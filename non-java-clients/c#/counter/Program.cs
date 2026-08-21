using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var counters = client.Counters();

// Define a bounded, persistent strong counter starting at 1
var strongConfig = new CounterConfiguration
{
    Type = CounterType.Strong,
    Bounded = true,
    InitialValue = 1,
    Storage = CounterStorage.Persistent,
    LowerBound = 0,
    UpperBound = 100
};
await counters.Define("strong-counter", strongConfig);
Console.WriteLine("Strong counter defined.");

var strong = counters.Counter("strong-counter");

var val = await strong.Get();
Console.WriteLine($"Get: {val}");

val = await strong.AddAndGet(10);
Console.WriteLine($"AddAndGet(10): {val}");

var (oldVal, swapped) = await strong.CompareAndSwap(11, 50);
Console.WriteLine($"CompareAndSwap(11 -> 50): old={oldVal}, swapped={swapped}");

await strong.Reset();
val = await strong.Get();
Console.WriteLine($"After reset: {val}");

// Define a volatile weak counter
var weakConfig = new CounterConfiguration
{
    Type = CounterType.Weak,
    InitialValue = 0,
    Storage = CounterStorage.Volatile
};
await counters.Define("weak-counter", weakConfig);
Console.WriteLine("\nWeak counter defined.");

var weak = counters.Counter("weak-counter");
val = await weak.AddAndGet(5);
Console.WriteLine($"Weak AddAndGet(5): {val}");

// List all counter names
var names = await counters.Names();
Console.WriteLine($"\nCounter names: {string.Join(", ", names)}");

// Cleanup
await counters.Remove("strong-counter");
await counters.Remove("weak-counter");
Console.WriteLine("Counters removed.");
