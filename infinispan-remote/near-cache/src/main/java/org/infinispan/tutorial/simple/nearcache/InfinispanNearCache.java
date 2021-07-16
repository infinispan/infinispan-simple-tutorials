package org.infinispan.tutorial.simple.nearcache;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.tutorial.simple.connect.Infinispan;

public class InfinispanNearCache {

   public static final String CACHE_WITH_NEAR_CACHING = "testCacheNearCaching";

   public static void main(String[] args) {
      ConfigurationBuilder builder = Infinispan.connectionConfig();
      // Add an additional cache with near caching configuration
      builder.remoteCache(CACHE_WITH_NEAR_CACHING)
            .configuration(Infinispan.TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", CACHE_WITH_NEAR_CACHING))
            .nearCacheMode(NearCacheMode.INVALIDATED)
            .nearCacheMaxEntries(20)
            .nearCacheUseBloomFilter(true);

      // Connect to the server with the near cache configuration for the test cache
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      RemoteCache<Integer, String> testCache = cacheManager.getCache(Infinispan.TUTORIAL_CACHE_NAME);
      RemoteCache<Integer, String> withNearCaching = cacheManager.getCache(CACHE_WITH_NEAR_CACHING);

      for (int i = 1; i<= 20; i++) {
         testCache.put(i, String.valueOf(i));
         withNearCaching.put(i, String.valueOf(i));
      }

      // Read both caches data
      readCache(testCache);
      readCache(withNearCaching);

      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

   private static void readCache(RemoteCache<Integer, String> cache) {
      Instant start = Instant.now();
      Random random = new Random();
      random.ints(10_000, 1, 20).forEach(num -> cache.get(num));
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      System.out.println(String.format("Time to complete with cache %s is %d milliseconds", cache.getName(), timeElapsed));
   }

}
