package org.infinispan.tutorial.simple.multimap;

import java.util.concurrent.CompletableFuture;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.multimap.api.embedded.EmbeddedMultimapCacheManagerFactory;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.infinispan.multimap.api.embedded.MultimapCacheManager;

public class InfinispanMultimap {

   public static void main(String[] args) {
      // Construct a local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();

      // Obtain a multimap cache manager from the regular cache manager
      MultimapCacheManager multimapCacheManager = EmbeddedMultimapCacheManagerFactory.from(cacheManager);

      // Define de multimap cache configuration, as a regular cache
      multimapCacheManager.defineConfiguration("multimap", new ConfigurationBuilder().build());

      // Get the MultimapCache
      MultimapCache<String, String> multimap = multimapCacheManager.get("multimap");

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
                  // Stop the cache manager and release all resources
                  cacheManager.stop();
               });
            });
   }

}
