package org.infinispan.tutorial.simple.remote.listen;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanRemoteListen {

   public static void main(String[] args) throws InterruptedException {
      // Connect to the server
      RemoteCacheManager cacheManager = TutorialsConnectorHelper.connect();

      // Get the test cache
      RemoteCache<String, String> cache = cacheManager.getCache(TutorialsConnectorHelper.TUTORIAL_CACHE_NAME);

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
      TutorialsConnectorHelper.stop(cacheManager);
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
