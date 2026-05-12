package org.infinispan.tutorial.simple.alias;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * Runs with Infinispan 15.1 or higher
 */
public class InfinispanCacheAlias {

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static RemoteCache<String, String> cacheAlias;

   public static void main(String[] args) {
      connectAndStore();
      addAlias();
      disconnect();
   }

   static void connectAndStore() {
      // tag::connect-and-store[]
      // Connect to the server
      cacheManager = TutorialsConnectorHelper.connect();
      // Obtain the remote cache
      cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);

      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // end::connect-and-store[]
   }

   static void addAlias() {
      // tag::aliases[]
      // Add aliases to the cache
      cacheManager.administration().updateConfigurationAttribute(cache.getName(), "aliases", "alias alias2");

      // Retrieve the value through an alias
      cacheAlias = cacheManager.getCache("alias");
      if (cacheAlias != null) {
         System.out.printf("key = %s\n", cacheAlias.get("key"));
      }
      // end::aliases[]
   }

   static void disconnect() {
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }

}
