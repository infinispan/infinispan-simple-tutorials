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
   public static final String DIST_CACHE_NAME = "cache";
   DefaultCacheManager cm1;
   Cache<String, String> cache;

   public static void main(String[] args) {
      InfinispanDistributed infinispanDistributed = new InfinispanDistributed();
      infinispanDistributed.createDefaultCacheManager();
      infinispanDistributed.createAndPopulateTheCache(10);
      infinispanDistributed.displayCacheContent();
      infinispanDistributed.stopDefaultCacheManager();
   }

   public void createDefaultCacheManager() {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      cm1 = new DefaultCacheManager(global.build());
   }

   public void createAndPopulateTheCache(int size) {
      //Create cache configuration
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.DIST_SYNC);
      // Create a cache
      cache = cm1.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache(DIST_CACHE_NAME, builder.build());
      // Store the current node address in some random keys
      for (int i = 0; i < size; i++) {
         cache.put(UUID.randomUUID().toString(), cm1.getNodeAddress());
      }
   }

   public void displayCacheContent() {
      if (cache == null) {
         System.out.println("The cache is null");
         return;
      }

      // Display the current cache contents for the whole cluster
      cache.entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
      // Display the current cache contents for this node
      // Note: By default numOwners=2, so in a cluster with 2 nodes, each node owns all the keys:
      // some of the keys as "primary owner" and some keys as "backup owner"
      cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).entrySet()
              .forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
   }

   public void stopDefaultCacheManager() {
      if (cm1 != null) {
         // Stop the cache manager and release all resources
         cm1.stop();
         cm1 = null;
      }
   }
}
