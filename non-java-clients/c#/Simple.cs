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
        static void Main() {
            // Create a configuration for a locally-running server
            ConfigurationBuilder builder = new ConfigurationBuilder();
            Configuration conf = builder.AddServers("127.0.0.1:11222").Build();
            // Initialize the remote cache manager
            RemoteCacheManager remoteManager = new RemoteCacheManager(conf);
            // Connect to the server
            remoteManager.Start();
            // Store a value
            IRemoteCache<string,string> cache=remoteManager.GetCache<string, string>();
            cache.Put("key", "value");
            // Retrieve the value and print it out
            Console.WriteLine("key = {0}", cache.Get("key"));
            // Stop the cache manager and release all resources
            remoteManager.Stop();
        }
    }
}
