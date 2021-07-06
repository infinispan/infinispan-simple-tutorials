package org.infinispan.tutorial.simple.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.tutorial.simple.connect.Infinispan;

import java.net.URI;

/**
 * Run the Infinispan Server using the Docker Image
 * Or make sure you create a user that is called 'admin'.
 * Check more about authorization in the Infinispan Documentation.
 */
public class InfinispanAuthorizationCache {

   public static void main(String[] args) throws Exception {
      ConfigurationBuilder configurationBuilder = Infinispan.connectionConfig();
      URI securedCacheConfig = InfinispanAuthorizationCache.class.getClassLoader().getResource("securedCache.xml").toURI();
      configurationBuilder.remoteCache("securedCache").configurationURI(securedCacheConfig);

      // Connect to Infinispan with an additional cache that is secured with authorization for
      // deployer role only. We are connecting with 'admin' user that has 'admin' role.
      RemoteCacheManager cacheManager = new RemoteCacheManager(configurationBuilder.build());
      RemoteCache<String, String> cache = cacheManager.getCache(Infinispan.TUTORIAL_CACHE_NAME);
      RemoteCache<String, String> securedCache = cacheManager.getCache("securedCache");

      // Store a value in a non secured cache
      cache.put("key", "value");

      try {
         // Store a value in a cache where your role is has not access granted
         securedCache.put("key", "value");
      } catch (HotRodClientException ex) {
         System.out.println(ex.getMessage());
      }

      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
