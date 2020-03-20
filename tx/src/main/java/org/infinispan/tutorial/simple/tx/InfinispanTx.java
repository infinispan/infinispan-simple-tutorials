package org.infinispan.tutorial.simple.tx;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

import javax.transaction.TransactionManager;

public class InfinispanTx {

   public static void main(String[] args) throws Exception {
      // Construct a local cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Create a transaction cache config
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      Configuration cacheConfig = builder.build();
      // Create a cache with the config
      Cache<String, String> cache = cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("cache", cacheConfig);
      // Obtain the transaction manager
      TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();
      // Perform some operations within a transaction and commit it
      transactionManager.begin();
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      transactionManager.commit();
      // Display the current cache contents
      System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
      // Perform some operations within a transaction and roll it back
      transactionManager.begin();
      cache.put("key1", "value3");
      cache.put("key2", "value4");
      transactionManager.rollback();
      // Display the current cache contents
      System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
