package org.infinispan.tutorial.simple.kubernetes;

import java.net.Inet4Address;
import java.net.UnknownHostException;
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
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

public class InfinispanKubernetes {

   public static void main(String[] args) throws UnknownHostException {
      //Configure Infinispan to use default transport and Kubernetes configuration
      GlobalConfiguration globalConfig = new GlobalConfigurationBuilder().transport()
            .defaultTransport()
            .addProperty("configurationFile", "default-configs/default-jgroups-kubernetes.xml")
            .build();


      // We need a distributed cache for the purpose of this demo
      Configuration cacheConfiguration = new ConfigurationBuilder()
            .clustering()
            .cacheMode(CacheMode.REPL_SYNC)
            .build();

      DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, cacheConfiguration);
      cacheManager.defineConfiguration("default", cacheConfiguration);
      Cache<String, String> cache = cacheManager.getCache("default");

      //Each cluster member will update its own entry in the cache
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
         //This container will operate for an hour and then it will die
         TimeUnit.HOURS.sleep(1);
      } catch (InterruptedException e) {
         scheduler.shutdown();
         cacheManager.stop();
      }
   }
}
