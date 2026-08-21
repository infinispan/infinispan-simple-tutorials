using Infinispan.Hotrod;
using Tutorial;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

const string instaPostProto = """
    syntax = "proto3";
    package tutorial;

    message InstaPost {
        string id = 1;
        string user = 2;
        string hashtag = 3;
    }
    """;

const string cacheConfig = """
    <distributed-cache name="test">
        <encoding media-type="application/x-protostream"/>
    </distributed-cache>
    """;

var users = new[] {
    "gustavoalle", "remerson", "anistor", "karesti", "ttarrant",
    "belen_esteban", "dberindei", "galderz", "wburns", "pruivo",
    "oliveira", "vrigamonti",
};
var hashtags = new[] {
    "love", "instagood", "photooftheday", "fashion", "beautiful",
    "happy", "cute", "tbt", "like4like", "followme", "infinispan",
};

// Register the InstaPost proto schema
await client.Administration().Schemas().CreateOrUpdate("instapost.proto", instaPostProto);
Console.WriteLine("Registered instapost.proto schema.");

var admin = client.Administration();
await admin.GetOrCreateCache("test", cacheConfig);

var cache = client.NewCache<InstaPost>("test")
    .WithEncoding(MediaType.Protobuf)
    .Build();

// TypedContinuousQuery auto-deserializes events using the cache's marshallers
var cqParams = new Dictionary<string, object> { ["userName"] = "belen_esteban" };
await using var cq = cache.TypedContinuousQuery(
    "FROM tutorial.InstaPost p WHERE p.user = :userName", cqParams);
Console.WriteLine("Continuous query registered.");

// Consume typed CQ events — no manual byte decoding needed
var matchCount = 0;
var consumer = Task.Run(async () =>
{
    await foreach (var ev in cq.Events.ReadAllAsync())
    {
        if (ev.Type == CQResultType.Joining)
        {
            Console.WriteLine($"@belen_esteban has posted again! Hashtag: #{ev.Value.Hashtag}");
            Interlocked.Increment(ref matchCount);
        }
    }
});

// Add 100 random posts
var rng = new Random(42);
const int numPosts = 100;
for (var i = 0; i < numPosts; i++)
{
    var id = $"post-{i}";
    var user = users[rng.Next(users.Length)];
    var hashtag = hashtags[rng.Next(hashtags.Length)];
    await cache.Put(id, new InstaPost { Id = id, User = user, Hashtag = hashtag });
    await Task.Delay(10);
}

// Wait briefly for remaining events
await Task.Delay(1000);

// Dispose the continuous query (removes the listener)
await cq.DisposeAsync();
await consumer;

Console.WriteLine($"\nTotal posts: {numPosts}");
Console.WriteLine($"Total posts by @belen_esteban: {matchCount}");

// Clean up
await admin.RemoveCache("test");
