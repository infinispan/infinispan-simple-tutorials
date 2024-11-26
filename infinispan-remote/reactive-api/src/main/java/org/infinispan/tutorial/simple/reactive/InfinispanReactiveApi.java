package org.infinispan.tutorial.simple.reactive;

import org.infinispan.api.Infinispan;
import org.infinispan.api.mutiny.MutinyCache;
import org.infinispan.api.sync.SyncCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

public class InfinispanReactiveApi {

   static Infinispan infinispan;
   static MutinyCache<String, String> cache;

   public static void main(String[] args) {
      connect();
      initCache();
      manipulateCacheReactive();
      disconnect(false);
   }

   static void connect() {
      // Connect to the server
      ConfigurationBuilder configurationBuilder = TutorialsConnectorHelper.connectionConfig();
      infinispan = Infinispan.create(configurationBuilder.create());
      cache = infinispan.mutiny().caches().<String, String>get(TUTORIAL_CACHE_NAME).await().atMost(Duration.ofSeconds(10));
   }

   static void initCache() {
      cache = infinispan.mutiny()
            .caches().<String, String>get(TUTORIAL_CACHE_NAME)
            .await().atMost(Duration.ofMillis(100));
   }

   static void manipulateCacheReactive() {
      ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

      cache.set("hello", "reactive")
              .chain(ignore -> cache.get("hello"))
              .onItem().invoke(v -> System.out.printf("%s -- %s\n", LocalDateTime.now(), v))
              .map(v -> v + " is nice!!")
              .onItem().delayIt().onExecutor(executor).by(Duration.ofSeconds(1))
              .invoke(v -> System.out.printf("%s -- %s\n", LocalDateTime.now(), v))
              .await().atMost(Duration.ofSeconds(2));

      executor.shutdown();
   }

   private static void clearCache() {
      if (infinispan != null) {
         // Clear the cache in case it already exists from a previous running tutorial
         SyncCache<Object, Object> cache = infinispan.sync().caches().get(TUTORIAL_CACHE_NAME);
         if (cache != null) {
            cache.clear();
         }
      }
   }

   public static void disconnect(boolean removeCache) {
      if (infinispan != null) {
         if (removeCache) {
            infinispan.sync().caches().remove(TUTORIAL_CACHE_NAME);
         }
         infinispan.close();
      }
   }

}
