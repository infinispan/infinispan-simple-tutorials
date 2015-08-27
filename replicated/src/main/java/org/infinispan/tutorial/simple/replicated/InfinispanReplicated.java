package org.infinispan.tutorial.simple.replicated;

import java.util.UUID;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanReplicated {

   public static void main(String[] args) throws Exception {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Make the default cache a replicated synchronous one
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.REPL_SYNC);
      // Initialize the cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager(global.build(), builder.build());
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();
      // Store the current node address in some random keys
      for(int i=0; i < 10; i++) {
         cache.put(UUID.randomUUID().toString(), cacheManager.getNodeAddress());
      }
      // Display the current cache contents for the whole cluster
      cache.entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
      // Display the current cache contents for this node
      cache.getAdvancedCache().withFlags(Flag.SKIP_REMOTE_LOOKUP)
         .entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
