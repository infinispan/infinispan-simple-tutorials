package org.infinispan.tutorial.simple.jcache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

public class InfinispanJCache {

   static CachingProvider jcacheProvider;
   static CacheManager cacheManager;
   static Cache<String, String> cache;
   
   public static void main(String[] args) {
      createAndStartComponents();
      manipulateCache();
      close();
   }

   static void manipulateCache() {
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
   }

   static void createAndStartComponents() {
      // Construct a simple local cache manager with default configuration
      jcacheProvider = Caching.getCachingProvider();
      cacheManager = jcacheProvider.getCacheManager();
      MutableConfiguration<String, String> configuration = new MutableConfiguration<>();
      configuration.setTypes(String.class, String.class);
      // create a cache using the supplied configuration
      cache = cacheManager.createCache("myCache", configuration);
   }

   static void close() {
      if (cacheManager != null) {
         // Stop the cache manager and release all resources
         cacheManager.close();
      }
   }
}
