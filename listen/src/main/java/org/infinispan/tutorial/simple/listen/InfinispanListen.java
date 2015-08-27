package org.infinispan.tutorial.simple.listen;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

public class InfinispanListen {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();
      // Register a listener
      cache.addListener(new MyListener());
      // Store some values
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      cache.put("key1", "newValue");
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

   @Listener
   public static class MyListener {

      @CacheEntryCreated
      public void entryCreated(CacheEntryCreatedEvent<String, String> event) {
         // We are only interested in the post event
         if (!event.isPre())
            System.out.printf("Created %s\n", event.getKey());
      }

      @CacheEntryModified
      public void entryModified(CacheEntryModifiedEvent<String, String> event) {
         // We are only interested in the pre event
         if (event.isPre())
            System.out.printf("About to modify %s\n", event.getKey());
      }
   }
}
