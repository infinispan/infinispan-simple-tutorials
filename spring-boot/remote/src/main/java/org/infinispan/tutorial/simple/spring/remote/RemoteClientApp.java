package org.infinispan.tutorial.simple.spring.remote;

import java.lang.invoke.MethodHandles;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * This example shows how to configure Spring Boot and Infinispan Server
 */
@SpringBootApplication
public class RemoteClientApp {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
   private static final String NEWBORNS_CACHE_NAME = "newborns";

   @Autowired
   private RemoteCacheManager cacheManager;

   @Autowired
   private RemoteCache<String, String> defaultCache;

   public static void main(String[] args) {
      SpringApplication.run(RemoteClientApp.class, args);
   }

   @Bean
   @Order(1)
   public CommandLineRunner putDataAndGetDataInTheDefaultCache(ApplicationContext ctx) {
      return args -> {
         defaultCache.put("key", "value");
         logger.info("Get 'key' from injected default cache: " + defaultCache.get("key"));
      };
   }

   @Bean
   @Order(2)
   public CommandLineRunner createCache(ApplicationContext ctx) {
      return args -> {
         cacheManager.administration().getOrCreateCache(NEWBORNS_CACHE_NAME, "default");
         logger.info(String.format("'%s' cache has been created", NEWBORNS_CACHE_NAME));
      };
   }

   @Bean
   @Order(3)
   public CommandLineRunner putAndGetDataFromCache(ApplicationContext ctx) {
      return args -> {
         RemoteCache<String, String> cache = cacheManager.getCache(NEWBORNS_CACHE_NAME);
         cache.put("name", "Elaia");
         logger.info(String.format("The baby's name is %s", cache.get("name")));
      };
   }
}
