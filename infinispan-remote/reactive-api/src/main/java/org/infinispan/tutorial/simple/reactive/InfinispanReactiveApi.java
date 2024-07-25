package org.infinispan.tutorial.simple.reactive;

import org.infinispan.api.Infinispan;
import org.infinispan.api.mutiny.MutinyCache;
import org.infinispan.commons.util.OS;
import org.infinispan.hotrod.configuration.ClientIntelligence;
import org.infinispan.hotrod.configuration.HotRodConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.HOST;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.INFINISPAN_CONTAINER;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.PASSWORD;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.SINGLE_PORT;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_CONFIG;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.USER;

public class InfinispanReactiveApi {

   static Infinispan infinispan;
   static MutinyCache<String, String> cache;

   public static void main(String[] args) {
      connect();
      initCache();
      manipulateCacheReactive();
      disconnect(false);
   }

   static void initCache() {
      cache = infinispan.mutiny()
            .caches().<String, String>get(TUTORIAL_CACHE_NAME)
            .await().atMost(Duration.ofMillis(100));
   }

   static void manipulateCacheReactive() {
      ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, t -> {
         Thread wrap = new Thread(t);
         wrap.setDaemon(true);
         return wrap;
      });

      cache.set("hello", "reactive")
              .chain(ignore -> cache.get("hello"))
              .onItem().invoke(v -> System.out.printf("%s -- %s\n", LocalDateTime.now(), v))
              .map(v -> v + " is nice!!")
              .onItem().delayIt().onExecutor(executor).by(Duration.ofSeconds(1))
              .invoke(v -> System.out.printf("%s -- %s\n", LocalDateTime.now(), v))
              .await().atMost(Duration.ofSeconds(2));
   }

   public static final void connect() {
      // New API Connection
      HotRodConfigurationBuilder builder = createHotRodConfigurationBuilder();
      // Add default host/port server
      builder.addServer().host(HOST).port(SINGLE_PORT);

      infinispan = null;
      try {
         infinispan = Infinispan.create(builder.build());
         clearCache();
      } catch (Exception ex) {
         System.out.println("Unable to connect to a running server in localhost:11222. Try test containers");
         if (infinispan != null) {
            disconnect(false);
         }
         infinispan = null;
      }

      if (infinispan == null) {
         try {
            TutorialsConnectorHelper.startInfinispanContainer();
            builder = createHotRodConfigurationBuilder();
            builder.addServer().host(HOST).port(INFINISPAN_CONTAINER.getFirstMappedPort());
            infinispan = Infinispan.create(builder.build());
            clearCache();
         } catch (Exception ex) {
            System.out.println("Infinispan Server start with Testcontainers failed. Exit");
            System.exit(0);
         }
      }
   }

   @NotNull
   private static HotRodConfigurationBuilder createHotRodConfigurationBuilder() {
      HotRodConfigurationBuilder builder = new HotRodConfigurationBuilder();
      if (OS.getCurrentOs().equals(OS.MAC_OS) || OS.getCurrentOs().equals(OS.WINDOWS)) {
         // This is for DEV MODE LOCAL !! Don't add this in production, you will hit performance issues
         builder.clientIntelligence(ClientIntelligence.BASIC);
      }

      builder.security().authentication()
              .username(USER)
              .password(PASSWORD);

      builder.remoteCache(TUTORIAL_CACHE_NAME)
              .configuration(TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", TUTORIAL_CACHE_NAME));
      return builder;
   }

   private static void clearCache() {
      if (infinispan != null) {
         // Clear the cache in case it already exists from a previous running tutorial
         infinispan.sync().caches().get(TUTORIAL_CACHE_NAME).clear();
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
