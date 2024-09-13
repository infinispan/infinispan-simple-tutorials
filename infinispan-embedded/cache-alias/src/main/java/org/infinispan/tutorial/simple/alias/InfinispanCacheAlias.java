package org.infinispan.tutorial.simple.alias;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManagerAdmin;

import java.util.UUID;

public class InfinispanCacheAlias {
   public static final String DIST_CACHE_NAME = "cache";
   public static final String ALIAS_1 = "alias1";
   public static final String ALIAS_2 = "alias2";
   public DefaultCacheManager cm1;
   Cache<String, String> cache;

   public static void main(String[] args) {
      InfinispanCacheAlias infinispanDistributed = new InfinispanCacheAlias();
      infinispanDistributed.createDefaultCacheManager();
      infinispanDistributed.createACacheWithAliasAndPopulate(1);
      infinispanDistributed.displayCacheContent(DIST_CACHE_NAME);
      infinispanDistributed.displayCacheContent(ALIAS_1);
      infinispanDistributed.updateCacheConfigWithSecondAlias();
      infinispanDistributed.displayCacheContent(ALIAS_2);
      infinispanDistributed.stopDefaultCacheManager();
   }

   public void createDefaultCacheManager() {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      cm1 = new DefaultCacheManager(global.build());
   }

   public void createACacheWithAliasAndPopulate(int size) {
      //Create cache configuration
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.DIST_SYNC);
      builder.aliases(ALIAS_1);

      // Create a cache
      cache = cm1.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache(DIST_CACHE_NAME, builder.build());
      // Store the current node address in some random keys
      for (int i = 0; i < size; i++) {
         cache.put(UUID.randomUUID().toString(), cm1.getNodeAddress());
      }
   }

   public void updateCacheConfigWithSecondAlias() {
      if (cm1 == null) {
         System.out.println("The cache manager is null");
         return;
      }

      EmbeddedCacheManagerAdmin administration = cm1.administration();
      administration.updateConfigurationAttribute(DIST_CACHE_NAME, "aliases", ALIAS_2);
   }

   public void displayCacheContent(String cacheName) {
      if (cm1 == null) {
         System.out.println("The cache manager is null");
         return;
      }

      System.out.println("Display content of cache: " + cacheName);
      cm1.getCache(cacheName)
              .entrySet()
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
