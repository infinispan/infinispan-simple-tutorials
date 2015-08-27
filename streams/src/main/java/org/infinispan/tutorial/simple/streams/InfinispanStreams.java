package org.infinispan.tutorial.simple.streams;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanStreams {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      // Obtain the default cache
      Cache<Integer, String> cache = cacheManager.getCache();
      // Store some values
      int range = 10;
      IntStream.range(0, range).boxed().forEach(i -> cache.put(i, i + "-value"));
      // Map and reduce the keys
      Integer result = cache.keySet().stream()
         .map((Serializable & Function<Integer, Integer>) e -> e)
         .reduce(0, (Serializable & BinaryOperator<Integer>) (e1, e2) -> e1 + e2);
      System.out.printf("Result = %d\n", result);
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
