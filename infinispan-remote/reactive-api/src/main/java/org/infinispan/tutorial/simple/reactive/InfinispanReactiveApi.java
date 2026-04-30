package org.infinispan.tutorial.simple.reactive;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.HOST;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.SINGLE_PORT;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.infinispan.api.Infinispan;
import org.infinispan.api.mutiny.MutinyCache;
import org.infinispan.api.sync.SyncCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.testcontainers.InfinispanContainer;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

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
      try {
         infinispan = Infinispan.create(configurationBuilder.create());
         //ping
         System.out.println("Get cache names: " + infinispan.sync().caches().names());
      } catch (Exception ex) {
         System.out.println("Unable to connect to a running server in localhost:11222. Try test containers");
         if (infinispan != null) {
            infinispan.close();
         }
         infinispan = null;
      }

      if (infinispan == null) {
         try {
            InfinispanContainer container = TutorialsConnectorHelper.startInfinispanContainer();
            configurationBuilder.addServer().host(HOST).port(container.getMappedPort(SINGLE_PORT));
            infinispan = Infinispan.create(configurationBuilder.create());
            //ping
            System.out.println("Get cache names: " + infinispan.sync().caches().names());
         } catch (Exception ex) {
            System.out.println("Infinispan Server start with Testcontainers failed. Exit");
            System.exit(0);
         }
      }

      if (infinispan == null) {
         throw new IllegalStateException("Could not connect");
      }

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
