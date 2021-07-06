package org.infinispan.tutorial.simple.jcache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

public class InfinispanJCache {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      CachingProvider jcacheProvider = Caching.getCachingProvider();
      CacheManager cacheManager = jcacheProvider.getCacheManager();
      MutableConfiguration<String, String> configuration = new MutableConfiguration<>();
      configuration.setTypes(String.class, String.class);
      // create a cache using the supplied configuration
      Cache<String, String> cache = cacheManager.createCache("myCache", configuration);
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.close();
   }

}
