package org.infinispan.tutorial.simple.listen;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfinispanListen {

   static DefaultCacheManager cacheManager;
   static Cache<String, String> cache;
   static MyListener listener;

   public static void main(String[] args) {
      createAndStartComponents();
      manipulateCache();
      stop();
   }

   static void manipulateCache() {
      // Store some values
      cache.put("key1", "value1");
      cache.put("key2", "value2");
      cache.put("key1", "newValue");
   }

   static void createAndStartComponents() {
      // Construct a simple local cache manager with default configuration
      cacheManager = new DefaultCacheManager();
      // Define local cache configuration
      cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
      // Obtain the local cache
      cache = cacheManager.getCache("local");

      listener = new MyListener();
      // Register a listener
      cache.addListener(listener);
   }

   static void stop() {
      if (cacheManager != null) {
         // Stop the cache manager and release all resources
         cacheManager.stop();
      }
   }

   @Listener
   public static class MyListener {
      Map<String, String> created = new HashMap();
      Map<String, String> updated = new HashMap<>();

      @CacheEntryCreated
      public void entryCreated(CacheEntryCreatedEvent<String, String> event) {
         // We are only interested in the post event
         if (!event.isPre())
            created.put(event.getKey(), event.getValue());
            System.out.printf("Created %s\n", event.getKey());
      }

      @CacheEntryModified
      public void entryModified(CacheEntryModifiedEvent<String, String> event) {
         // We are only interested in the pre event
         if (event.isPre())
            updated.put(event.getKey(), event.getNewValue());
            System.out.printf("About to modify %s\n", event.getKey());
      }
   }
}
