package org.infinispan.tutorial.simple.map;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanMap {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
