using Infinispan.HotRod.Impl;
using Infinispan.HotRod.Config;
using Infinispan.HotRod;
using System;
using System.IO;
using System.Collections.Generic;
using System.Threading;

namespace Infinispan.Tutorial
{
    public class Simple
    {
        static void main() {
            // Create a configuration for a locally-running server
            ConfigurationBuilder builder = new ConfigurationBuilder();
            conf = builder.AddServers("127.0.0.1:11222").Build();
            // Initialize the remote cache manager
            remoteManager = new RemoteCacheManager(conf, new DefaultSerializer());
            // Connect to the server
            remoteManager.Start();
            // Store a value
            cache.Put("key", "value");
            // Retrieve the value and print it out
            Console.WriteLine("key = {0}", cache.Get("key"));
            // Stop the cache manager and release all resources
            remoteManager.Stop();
        }
    }
}
