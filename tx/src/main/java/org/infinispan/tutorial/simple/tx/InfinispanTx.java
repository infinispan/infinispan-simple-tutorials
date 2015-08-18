package org.infinispan.tutorial.simple.tx;

import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;

public class InfinispanTx {

   public static void main(String[] args) throws Exception {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      DefaultCacheManager cacheManager = new DefaultCacheManager(builder.build());
      Cache<String, String> cache = cacheManager.getCache();
      TransactionManager transactionManager = cache.getAdvancedCache().getTransactionManager();
      transactionManager.begin();
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      transactionManager.commit();
      System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
      transactionManager.begin();
      cache.put("key1", "value3");
      cache.put("key2", "value4");
      transactionManager.rollback();
      System.out.printf("key1 = %s\nkey2 = %s\n", cache.get("key1"), cache.get("key2"));
      cacheManager.stop();
   }

}
