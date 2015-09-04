package org.infinispan.tutorial.simple.remote.listen;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class InfinispanRemoteListen {

   public static void main(String[] args) throws InterruptedException {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Obtain the remote cache
      RemoteCache<String, String> cache = cacheManager.getCache();
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
