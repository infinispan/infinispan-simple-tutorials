using System.Text;
using Infinispan.Hotrod;

var uri = Environment.GetEnvironmentVariable("INFINISPAN_URI")
    ?? "hotrod://admin:password@localhost:11222";

var client = InfinispanClient.FromUri(uri);
client.ClientIntelligence = ClientIntelligence.Basic;

var admin = client.Administration();
await admin.GetOrCreateCache("listened");

var cache = client.NewCache(new StringMarshaller(), new StringMarshaller(), "listened");

// Register a listener for cache events
var listener = new EventPrinter();
await cache.AddListener(listener);
Console.WriteLine("Listener registered.");

// Produce events
await cache.Put("key1", "a");
await cache.Put("key2", "b");
await cache.Put("key1", "c"); // modify

await Task.Delay(500); // allow events to arrive

// Remove listener
await cache.RemoveListener(listener);
Console.WriteLine("Listener removed.");

// Cleanup
await admin.RemoveCache("listened");

class EventPrinter : IClientListener
{
    public string ListenerID { get; set; }

    public void OnEvent(Event e)
    {
        var key = Encoding.UTF8.GetString(e.Key);
        Console.WriteLine($"{e.Type} {key}");
    }

    public void OnError(Exception ex) { }
}
