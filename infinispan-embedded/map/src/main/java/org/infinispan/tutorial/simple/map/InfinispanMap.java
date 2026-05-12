package org.infinispan.tutorial.simple.map;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanMap {

   static DefaultCacheManager cacheManager;
   static Cache<String, String> cache;

   public static void main(String[] args) {
      createAndStartComponents();
      manipulateLocalCache();
      stop();
   }

   static void manipulateLocalCache() {
      // tag::put-get[]
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // end::put-get[]
   }

   static void createAndStartComponents() {
      // tag::config[]
      // Construct a simple local cache manager with default configuration
      cacheManager = new DefaultCacheManager();
      // Define local cache configuration
      cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
      // Obtain the local cache
      cache = cacheManager.getCache("local");
      // end::config[]
   }

   static void stop() {
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }
}
