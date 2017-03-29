package org.infinispan.tutorial.simple.map;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanMap {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Define local cache configuration
      cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
      // Obtain the local cache
      Cache<String, String> cache = cacheManager.getCache("local");
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
