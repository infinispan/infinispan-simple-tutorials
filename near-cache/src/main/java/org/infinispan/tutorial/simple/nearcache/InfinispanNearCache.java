package org.infinispan.tutorial.simple.nearcache;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class InfinispanNearCache {

   public static void main(String[] args) {
      // Create a client configuration connecting to a local server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
      builder.nearCache().mode(NearCacheMode.INVALIDATED).maxEntries(20).cacheNamePattern("near-.*");

      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      // Create one remote cache with near caching disabled and one with near caching enabled
      RemoteCache<Integer, String> numbers = cacheManager.administration().getOrCreateCache("numbers", DefaultTemplate.DIST_SYNC);
      RemoteCache<Integer, String> nearNumbers = cacheManager.administration().getOrCreateCache("near-numbers", DefaultTemplate.DIST_SYNC);

      for (int i = 1; i<= 20; i++) {
         numbers.put(i, String.valueOf(i));
         nearNumbers.put(i, String.valueOf(i));
      }

      // Read both caches data
      readCache(numbers);
      readCache(nearNumbers);

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
