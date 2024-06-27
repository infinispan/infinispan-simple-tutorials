package org.infinispan.tutorial.simple.reactive;

import org.infinispan.api.Infinispan;
import org.infinispan.api.mutiny.MutinyCache;
import org.infinispan.commons.util.OS;
import org.infinispan.hotrod.configuration.ClientIntelligence;
import org.infinispan.hotrod.configuration.HotRodConfigurationBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.HOST;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.PASSWORD;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.SINGLE_PORT;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_CONFIG;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.USER;

public class InfinispanReactiveApi {

   public static void main(String[] args) {
      // New API Connection
      HotRodConfigurationBuilder builder = new HotRodConfigurationBuilder();
      if (OS.getCurrentOs().equals(OS.MAC_OS) || OS.getCurrentOs().equals(OS.WINDOWS)) {
         // This is for DEV MODE LOCAL !! Don't add this in production, you will hit performance issues
         builder.clientIntelligence(ClientIntelligence.BASIC);
      }

      builder
            .addServer().host(HOST).port(SINGLE_PORT)
            .security().authentication()
            .username(USER)
            .password(PASSWORD);
      builder.remoteCache(TUTORIAL_CACHE_NAME)
            .configuration(TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", TUTORIAL_CACHE_NAME));

      Infinispan infinispan = Infinispan.create(builder.build());
      MutinyCache<String, String> cache = infinispan.mutiny()
            .caches().<String, String>get(TUTORIAL_CACHE_NAME)
            .await().atMost(Duration.ofMillis(100));

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

}
