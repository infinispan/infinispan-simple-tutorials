package org.infinispan.tutorial.simple.streams;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanStreams {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Define local cache configuration
      cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
      // Obtain the local cache
      Cache<String, String> cache = cacheManager.getCache("local");
      // Store some values
      int range = 10;
      IntStream.range(0, range).boxed().forEach(i -> cache.put(i + "-key", i + "-value"));
      // Map and reduce the keys
      int result = cache.keySet().stream()
            .map(e -> Integer.valueOf(e.substring(0, e.indexOf("-"))))
            .collect(() -> Collectors.summingInt(i -> i.intValue()));
      System.out.printf("Result = %d\n", result);
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
