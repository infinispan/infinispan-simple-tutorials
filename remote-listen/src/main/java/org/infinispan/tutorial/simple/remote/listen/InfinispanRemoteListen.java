package org.infinispan.tutorial.simple.remote.listen;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanRemoteListen {

   public static void main(String[] args) throws InterruptedException {
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
      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Get the cache, create it if needed with an existing template name
      RemoteCache<String, String> cache = cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("listen", DefaultTemplate.DIST_SYNC);
      // Register a listener
      MyListener listener = new MyListener();
      cache.addClientListener(listener);
      // Store some values
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      cache.put("key1", "newValue");
      // Remote events are asynchronous, so wait a bit
      Thread.sleep(1000);
      // Remove listener
      cache.removeClientListener(listener);
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

   @ClientListener
   public static class MyListener {

      @ClientCacheEntryCreated
      public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
         System.out.printf("Created %s%n", event.getKey());
      }

      @ClientCacheEntryModified
      public void entryModified(ClientCacheEntryModifiedEvent<String> event) {
         System.out.printf("About to modify %s%n", event.getKey());
      }

   }

}
