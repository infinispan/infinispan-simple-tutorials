package org.infinispan.tutorial.simple.alias;

import org.infinispan.api.configuration.CacheConfiguration;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.Set;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanCacheAlias {

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static RemoteCache<String, String> cacheAlias;

   public static void main(String[] args) {
      connectToInfinispan();
      manipulateCache();
      addAlias();
      disconnect();
   }

   static void manipulateCache() {
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
   }

   static void addAlias() {
      cacheManager.administration().updateConfigurationAttribute(cache.getName(), "aliases", "alias alias2");
      // Retrieve the value and print it out
      cacheAlias = cacheManager.getCache("alias");
      System.out.printf("key = %s\n", cacheAlias.get("key"));
   }

   static void connectToInfinispan() {
      // Connect to the server
      cacheManager = TutorialsConnectorHelper.connect();
      // Obtain the remote cache
      cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
   }

   static void disconnect() {
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }

}
