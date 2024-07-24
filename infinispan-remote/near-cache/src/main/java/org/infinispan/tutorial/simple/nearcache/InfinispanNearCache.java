package org.infinispan.tutorial.simple.nearcache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

public class InfinispanNearCache {

   public static final String CACHE_WITH_NEAR_CACHING = "testCacheNearCaching";

   static RemoteCacheManager cacheManager;
   static RemoteCache<Integer, String> testCache;
   static RemoteCache<Integer, String> withNearCaching;

   public static void main(String[] args) {
      connectToInfinispan();

      for (int i = 1; i<= 20; i++) {
         testCache.put(i, String.valueOf(i));
         withNearCaching.put(i, String.valueOf(i));
      }

      // Read both caches data
      readCache(testCache);
      readCache(withNearCaching);

      disconnect(false);
   }

   static void readCache(RemoteCache<Integer, String> cache) {
      Instant start = Instant.now();
      Random random = new Random();
      random.ints(10_000, 1, 20).forEach(num -> cache.get(num));
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      System.out.println(String.format("Time to complete with cache %s is %d milliseconds", cache.getName(), timeElapsed));
   }

   public static void connectToInfinispan() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      // Add an additional cache with near caching configuration
      builder.remoteCache(CACHE_WITH_NEAR_CACHING)
              .configuration(TutorialsConnectorHelper.TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", CACHE_WITH_NEAR_CACHING))
              .nearCacheMode(NearCacheMode.INVALIDATED)
              .nearCacheMaxEntries(20)
              .nearCacheUseBloomFilter(true);

      // Connect to the server with the near cache configuration for the test cache
      cacheManager = TutorialsConnectorHelper.connect(builder);
      testCache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
      withNearCaching = cacheManager.getCache(CACHE_WITH_NEAR_CACHING);
   }

   public static void disconnect(boolean removeCache) {
      if (removeCache) {
         cacheManager.administration().removeCache(TUTORIAL_CACHE_NAME);
         cacheManager.administration().removeCache(CACHE_WITH_NEAR_CACHING);
      }
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }
}
