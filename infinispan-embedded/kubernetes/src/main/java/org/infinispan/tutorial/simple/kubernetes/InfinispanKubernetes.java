package org.infinispan.tutorial.simple.kubernetes;

import java.io.IOException;
import java.net.Inet4Address;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanKubernetes {

   public static void main(String[] args) {
      System.out.println("Starting Embedded Infinispan Application...");
      //Configure Infinispan to use default transport and Kubernetes configuration
      GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
	    .transport()
            .defaultTransport()
            .addProperty("configurationFile", "default-configs/default-jgroups-kubernetes.xml")
            .build();

      try (DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig)) {
         // We need a distributed cache for the purpose of this demo
         Configuration cacheConfiguration = new ConfigurationBuilder()
               .clustering()
               .cacheMode(CacheMode.REPL_SYNC)
               .build();

         Cache<String, String> cache = cacheManager.createCache("kubernetes", cacheConfiguration);

         // Each cluster member will update its own entry in the cache
         String hostname = Inet4Address.getLocalHost().getHostName();
         ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
         scheduler.scheduleAtFixedRate(() -> {
                  String time = Instant.now().toString();
                  cache.put(hostname, time);
                  System.out.println("[" + time + "][" + hostname + "] Values from the cache: ");
                  System.out.println(cache.entrySet());
               },
               0, 2, TimeUnit.SECONDS);

         try {
            // Execute indefinitely until the process is interrupted by Kubernetes
            Thread.currentThread().join();
         } catch (InterruptedException e) {
            scheduler.shutdown();
            cacheManager.stop();
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
