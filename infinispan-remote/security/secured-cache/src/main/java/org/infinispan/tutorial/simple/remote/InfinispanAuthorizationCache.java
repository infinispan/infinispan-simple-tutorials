package org.infinispan.tutorial.simple.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 * Run the Infinispan Server using the Docker Image
 * Or make sure you create a user that is called 'admin'.
 * Check more about authorization in the Infinispan Documentation.
 */
public class InfinispanAuthorizationCache {

   public static final String SECURED_CACHE = "securedCache";
   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static RemoteCache<String, String> securedCache;
   static String message = "";

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      manipulateCache();
      disconnect(false);
   }

   static void manipulateCache() {
      // Store a value in a non secured cache
      cache.put("key", "value");

      try {
         // Store a value in a cache where your role is has not access granted
         securedCache.put("key", "value");
      } catch (HotRodClientException ex) {
         message = ex.getMessage().toLowerCase();
         System.out.println(ex.getMessage());
      }
   }

   public static void connectToInfinispan() throws Exception {
      ConfigurationBuilder configurationBuilder = TutorialsConnectorHelper.connectionConfig();
      URI securedCacheConfig = InfinispanAuthorizationCache.class.getClassLoader().getResource("securedCache.xml").toURI();
      configurationBuilder.remoteCache(SECURED_CACHE).configurationURI(securedCacheConfig);

      // Connect to Infinispan with an additional cache that is secured with authorization for
      // deployer role only. We are connecting with 'admin' user that has 'admin' role.
      cacheManager = TutorialsConnectorHelper.connect(configurationBuilder);
      cache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
      securedCache = cacheManager.getCache(SECURED_CACHE);
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches && cacheManager != null) {
         cacheManager.administration().removeCache(TUTORIAL_CACHE_NAME);
         cacheManager.administration().removeCache(SECURED_CACHE);
      }
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }
}
