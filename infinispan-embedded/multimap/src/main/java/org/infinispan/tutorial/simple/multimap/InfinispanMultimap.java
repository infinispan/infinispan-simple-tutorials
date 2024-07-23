package org.infinispan.tutorial.simple.multimap;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.multimap.api.embedded.EmbeddedMultimapCacheManagerFactory;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.multimap.api.embedded.MultimapCacheManager;

public class InfinispanMultimap {

   static DefaultCacheManager cacheManager;
   static MultimapCacheManager multimapCacheManager;
   static MultimapCache<String, String> multimap;

   public static void main(String[] args) throws Exception {
       createAndStartComponents();

       manipulateMultimap();
       stop();
   }

   static void manipulateMultimap() throws InterruptedException, ExecutionException, TimeoutException {
        // Store multiple values in a key
        CompletableFuture.allOf(
              multimap.put("key", "value1"),
              multimap.put("key", "value2"),
              multimap.put("key", "value3"))
              .whenComplete((nil, ex) -> {
                 // Retrieve the values
                 multimap.get("key").whenComplete((values, ex2) -> {
                    // Print them out
                    System.out.println(values);

                 });
              }).get(10, TimeUnit.SECONDS);
    }

    static void createAndStartComponents() {
        // Construct a local cache manager with default configuration
        cacheManager = new DefaultCacheManager();

        // Obtain a multimap cache manager from the regular cache manager
        multimapCacheManager = EmbeddedMultimapCacheManagerFactory.from(cacheManager);

        // Define de multimap cache configuration, as a regular cache
        multimapCacheManager.defineConfiguration("multimap", new ConfigurationBuilder().build());

        // Get the MultimapCache
        multimap = multimapCacheManager.get("multimap");
    }

    static void stop() {
       if (cacheManager != null) {
           // Stop the cache manager and release all resources
           cacheManager.stop();
       }
    }

}
