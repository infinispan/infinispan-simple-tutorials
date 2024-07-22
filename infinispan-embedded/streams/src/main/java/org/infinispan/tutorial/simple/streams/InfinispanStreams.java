package org.infinispan.tutorial.simple.streams;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InfinispanStreams {

   DefaultCacheManager cm1;
   Cache<String, String> cache;

   public static void main(String[] args) {
      InfinispanStreams streams = new InfinispanStreams();
      streams.createDefaultCacheManagerAndInitCache();
      streams.storeKeyValues(10);
      int result = streams.mapAndReduceKeys();
      streams.printResult(result);
      streams.stopDefaultCacheManager();
   }

   public void storeKeyValues(int range) {
      // Store some values
      IntStream.range(0, range).boxed().forEach(i -> cache.put(i + "-key", i + "-value"));
   }

   public int mapAndReduceKeys() {
      // Map and reduce the keys
      return cache.keySet().stream()
              .map(e -> Integer.valueOf(e.substring(0, e.indexOf("-"))))
              .collect(() -> Collectors.summingInt(i -> i.intValue()));
   }

   public void printResult(int result) {
      System.out.printf("Result = %d\n", result);
   }

   public void createDefaultCacheManagerAndInitCache() {
      // Construct a simple local cache manager with default configuration
      cm1 = new DefaultCacheManager();
      // Define local cache configuration
      cm1.defineConfiguration("local", new ConfigurationBuilder().build());
      // Obtain the local cache
      cache = cm1.getCache("local");
   }

   public void stopDefaultCacheManager() {
      if (cm1 != null) {
         // Stop the cache manager and release all resources
         cm1.stop();
         cm1 = null;
      }
   }
}
