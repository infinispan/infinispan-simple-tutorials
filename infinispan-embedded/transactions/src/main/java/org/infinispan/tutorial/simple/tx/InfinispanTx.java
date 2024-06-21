package org.infinispan.tutorial.simple.tx;

import jakarta.transaction.TransactionManager;
import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

import java.util.Map;

public class InfinispanTx {
   public static final String KEY_1 = "key1";
   public static final String KEY_2 = "key2";
   public static final String VALUE_1 = "value1";
   public static final String VALUE_2 = "value2";
   public static final String VALUE_3 = "value3";
   public static final String VALUE_4 = "value4";
   public static final String CACHE_NAME = "cache";
   DefaultCacheManager cm1;
   Cache<String, String> cache;

   public static void main(String[] args) throws Exception {
      InfinispanTx infinispanTx = new InfinispanTx();
      infinispanTx.createDefaultCacheManagerAndInitCache();
      infinispanTx.putAndCommit(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2));
      infinispanTx.displayCacheContent();
      infinispanTx.putAndRollback(Map.of(KEY_1, VALUE_3, KEY_2, VALUE_4));
      infinispanTx.displayCacheContent();
      infinispanTx.stopDefaultCacheManager();
   }

   public void createDefaultCacheManagerAndInitCache() {
      // Initialize the cache manager
      cm1 = new DefaultCacheManager();
      // Create a transaction cache config
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      Configuration cacheConfig = builder.build();
      // Create a cache with the config
      cache = cm1.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache(CACHE_NAME, cacheConfig);
   }

   private void displayCacheContent() {
      System.out.printf("key1 = %s\nkey2 = %s\n", cache.get(KEY_1), cache.get(KEY_2));
   }


   public void putAndCommit(Map<String, String> keyValues) throws Exception {
      // Obtain the transaction manager
      TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();
      // Perform some operations within a transaction and commit it
      transactionManager.begin();
      keyValues.forEach((k, v) -> {
         cache.put(k, v);
      });
      transactionManager.commit();
   }

   public void putAndRollback(Map<String, String> keyValues) throws Exception {
      // Obtain the transaction manager
      TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();
      // Perform some operations within a transaction and commit it
      transactionManager.begin();
      keyValues.forEach((k, v) -> {
         cache.put(k, v);
      });
      transactionManager.rollback();
   }

   public void stopDefaultCacheManager() {
      if (cm1 != null) {
         // Stop the cache manager and release all resources
         cm1.stop();
         cm1 = null;
      }
   }

}
