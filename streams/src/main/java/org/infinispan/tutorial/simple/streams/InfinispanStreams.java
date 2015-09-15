package org.infinispan.tutorial.simple.streams;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.stream.CacheCollectors;

public class InfinispanStreams {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();
      // Store some values
      int range = 10;
      IntStream.range(0, range).boxed().forEach(i -> cache.put(i + "-key", i + "-value"));
      // Map and reduce the keys
      int result = cache.keySet().stream()
         .map((Serializable & Function<String, Integer>) e -> Integer.valueOf(e.substring(0, e.indexOf("-"))))
              .collect(CacheCollectors.serializableCollector(() -> Collectors.summingInt(i -> i.intValue())));
      System.out.printf("Result = %d\n", result);
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
