package org.infinispan.tutorial.simple.remote.transaction;

import jakarta.transaction.TransactionManager;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;

/**
 * The Hot Rod transaction simple tutorial.
 * <p>
 * Hot Rod Transactions are available as of Infinispan version 9.3.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author Pedro Ruivo
 */
public class InfinispanRemoteTx {

   static final String CACHE_NAME = "simple-tx-cache";

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      manipulateWithTx();
      disconnect(false);
   }

   static void manipulateWithTx() throws Exception {
      // Obtain the transaction manager
      TransactionManager transactionManager = cache.getTransactionManager();
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
   }

   public static void connectToInfinispan() throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      // Add a transactional cache on startup
      URI cacheConfig = InfinispanRemoteTx.class.getClassLoader().getResource("simple-tx-cache.xml").toURI();
      builder.remoteCache(CACHE_NAME)
              // The cache that will be created is transactional
              .configurationURI(cacheConfig)
              // Use the simple TransactionManager in hot rod client
              .transactionManagerLookup(RemoteTransactionManagerLookup.getInstance())
              // The cache will be enlisted as Synchronization
              .transactionMode(TransactionMode.NON_XA);

      // Connect to the server
      cacheManager = TutorialsConnectorHelper.connect(builder);
      cache = cacheManager.getCache(CACHE_NAME);
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         cacheManager.administration().removeCache(CACHE_NAME);
      }
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }
}
