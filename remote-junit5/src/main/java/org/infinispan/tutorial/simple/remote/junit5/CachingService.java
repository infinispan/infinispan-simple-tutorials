package org.infinispan.tutorial.simple.remote.junit5;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

/**
 * Caching service to be tested using JUnit 5 and the Infinispan Server Extension
 */
public class CachingService {

   private RemoteCache<String, String> cache;

   public CachingService(RemoteCache<String, String> cache) {
      this.cache = cache;
   }

   public CachingService(RemoteCacheManager cacheManager) {
      this.cache = cacheManager.administration()
            .getOrCreateCache("names", DefaultTemplate.DIST_SYNC);
   }

   public void storeName(String id, String name) {
      cache.put(id, name);
   }

   public boolean exists(String name) {
      return cache.containsValue(name);
   }
}
