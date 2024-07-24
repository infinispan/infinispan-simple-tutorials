package org.infinispan.tutorial.simple.remote.listen;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanRemoteListen {

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static MyListener listener;

   public static void main(String[] args) throws InterruptedException {
      connectToInfinispan();

      registerListener();

      manipulateCache();

      // Remote events are asynchronous, so wait a bit
      Thread.sleep(1000);
      disconnect(false);
   }

   static void manipulateCache() {
      // Store some values
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      cache.put("key1", "newValue");
   }

   static void registerListener() {
      // Register a listener
      listener = new MyListener();
      cache.addClientListener(listener);
   }

   public static void connectToInfinispan() {
      // Connect to the server
      cacheManager = TutorialsConnectorHelper.connect();

      // Get the test cache
      cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
   }

   public static void disconnect(boolean removeCache) {
      // Remove listener
      cache.removeClientListener(listener);

      if (removeCache) {
         cacheManager.administration().removeCache(TUTORIAL_CACHE_NAME);
      }

      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }

   @ClientListener
   public static class MyListener {
      StringBuilder logTrack = new StringBuilder();

      @ClientCacheEntryCreated
      public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
         String logMessage = String.format("Created %s%n", event.getKey());
         logTrack.append(logMessage);
         System.out.printf(logMessage);
      }

      @ClientCacheEntryModified
      public void entryModified(ClientCacheEntryModifiedEvent<String> event) {
         String logMessage = String.format("About to modify %s%n", event.getKey());
         logTrack.append(logMessage);
         System.out.printf(logMessage);
      }

   }

}
