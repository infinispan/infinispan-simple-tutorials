package org.infinispan.tutorial.simple.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanRemoteCache {

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;

   public static void main(String[] args) {
      connectToInfinispan();
      manipulateCache();
      deconnect();
   }

   static void manipulateCache() {
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
   }

   static void connectToInfinispan() {
      // Connect to the server
      cacheManager = TutorialsConnectorHelper.connect();
      // Obtain the remote cache
      cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
   }

   static void deconnect() {
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }

}
