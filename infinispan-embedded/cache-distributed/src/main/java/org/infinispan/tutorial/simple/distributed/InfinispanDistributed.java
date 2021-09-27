package org.infinispan.tutorial.simple.distributed;

import java.util.UUID;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanDistributed {

   public static void main(String[] args) throws Exception {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());
      //Create cache configuration
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.DIST_SYNC);
      // Obtain a cache
      Cache<String, String> cache = cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
            .getOrCreateCache("cache", builder.build());

      // Store the current node address in some random keys
      for (int i = 0; i < 10; i++) {
         cache.put(UUID.randomUUID().toString(), cacheManager.getNodeAddress());
      }
      // Display the current cache contents for the whole cluster
      cache.entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
      // Display the current cache contents for this node
      // Note: By default numOwners=2, so in a cluster with 2 nodes, each node owns all the keys:
      // some of the keys as "primary owner" and some keys as "backup owner"
      cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).entrySet()
            .forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
