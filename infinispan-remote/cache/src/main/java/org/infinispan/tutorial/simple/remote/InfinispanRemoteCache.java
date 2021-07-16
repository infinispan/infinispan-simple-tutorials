package org.infinispan.tutorial.simple.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.tutorial.simple.connect.Infinispan;

import static org.infinispan.tutorial.simple.connect.Infinispan.TUTORIAL_CACHE_NAME;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanRemoteCache {

   public static void main(String[] args) {
      // Connect to the server
      RemoteCacheManager cacheManager = Infinispan.connect();
      // Obtain the remote cache
      RemoteCache<String, String> cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
      /// Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
