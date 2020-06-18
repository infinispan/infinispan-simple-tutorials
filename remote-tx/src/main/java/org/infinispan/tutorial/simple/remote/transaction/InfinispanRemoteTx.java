package org.infinispan.tutorial.simple.remote.transaction;

import javax.transaction.TransactionManager;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup;
import org.infinispan.commons.configuration.XMLStringConfiguration;

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

   private static final String CACHE_NAME = "simple-tx-cache";
   private static final String TEST_CACHE_XML_CONFIG =
         "<infinispan><cache-container>" +
               "  <local-cache-configuration name=\"" + CACHE_NAME + "\">" +
               "    <locking isolation=\"REPEATABLE_READ\"/>" +
               "    <transaction locking=\"PESSIMISTIC\" mode=\"NON_XA\" />" +
               "  </local-cache-configuration>" +
               "</cache-container></infinispan>";


   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
               .host("127.0.0.1")
               .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
             .security().authentication()
               //Add user credentials.
               .username("username")
               .password("password")
               .realm("default")
               .saslMechanism("DIGEST-MD5");

      // Configure the RemoteCacheManager to use a transactional cache as default
      // Use the simple TransactionManager in hot rod client
      builder.transaction().transactionManagerLookup(RemoteTransactionManagerLookup.getInstance());
      // The cache will be enlisted as Synchronization
      builder.transaction().transactionMode(TransactionMode.NON_XA);

      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      // Create a transactional cache in the server since there is none available by default.
      cacheManager.administration().createCache(CACHE_NAME, new XMLStringConfiguration(TEST_CACHE_XML_CONFIG));
      RemoteCache<String, String> cache = cacheManager.getCache(CACHE_NAME);

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
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
