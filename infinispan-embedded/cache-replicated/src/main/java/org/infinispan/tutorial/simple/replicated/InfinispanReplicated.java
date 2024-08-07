package org.infinispan.tutorial.simple.replicated;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.manager.DefaultCacheManager;

import java.util.UUID;

public class InfinispanReplicated {
   static DefaultCacheManager cacheManager;
   static Cache<String, String> cache;

   public static void main(String[] args) throws Exception {
      InfinispanReplicated.createDefaultCacheManagerAndStartCache();
      InfinispanReplicated.manipulateReplicatedCache();
      InfinispanReplicated.stopDefaultCacheManager();
   }

   public static void createDefaultCacheManagerAndStartCache() {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      cacheManager = new DefaultCacheManager(global.build());
      // Create a replicated synchronous configuration
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.REPL_SYNC);
      Configuration cacheConfig = builder.build();
      // Create a cache
      cache = cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("cache", cacheConfig);
   }

   public static void manipulateReplicatedCache() {
      // Store the current node address in some random keys
      for(int i=0; i < 10; i++) {
         cache.put(UUID.randomUUID().toString(), cacheManager.getNodeAddress());
      }

      // Display the current cache contents for the whole cluster
      cache.entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));

      // Display the current cache contents for this node
      cache.getAdvancedCache().withFlags(Flag.SKIP_REMOTE_LOOKUP)
              .entrySet().forEach(entry -> System.out.printf("%s = %s\n", entry.getKey(), entry.getValue()));
   }

   public static void stopDefaultCacheManager() {
      if (cacheManager != null) {
         // Stop the cache manager and release all resources
         cacheManager.stop();
      }
   }
}
