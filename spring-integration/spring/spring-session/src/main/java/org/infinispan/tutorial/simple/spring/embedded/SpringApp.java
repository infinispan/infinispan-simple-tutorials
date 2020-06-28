package org.infinispan.tutorial.simple.spring.embedded;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This tutorial shows how to run Spring Session with Infinispan and Spring Boot.
 */
@SpringBootApplication
@EnableInfinispanEmbeddedHttpSession
public class SpringApp {

   // Configure the sessions cache
   @Bean
   public InfinispanCacheConfigurer cacheConfigurer() {
      return manager -> {
         final Configuration ispnConfig = new ConfigurationBuilder()
               .clustering()
               .cacheMode(CacheMode.DIST_SYNC)
               .build();


         manager.defineConfiguration("sessions", ispnConfig);
      };
   }

   @Bean
   public InfinispanGlobalConfigurer globalCustomizer() {
      return () -> {
         GlobalConfigurationBuilder builder = GlobalConfigurationBuilder.defaultClusteredBuilder();
         builder.serialization().marshaller(new JavaSerializationMarshaller());
         builder.serialization().whiteList().addClass("org.springframework.session.MapSession");
         builder.serialization().whiteList().addRegexp("java.util.*");
         return builder.build();
      };
   }

   /**
    * Access http://localhost:8080/session and display the active sessions
    */
   @RestController
   static class SessionController {

      @Autowired
      SpringEmbeddedCacheManager cacheManager;

      @RequestMapping("/session")
      public Map<String, String> session(HttpServletRequest request) {
         Map<String, String> result = new HashMap<>();
         String sessionId = request.getSession(true).getId();
         result.put("created:", sessionId);
         // By default Infinispan integration for Spring Session will use 'sessions' cache.
         result.put("active:", cacheManager.getCache("sessions").getNativeCache().keySet().toString());
         return result;
      }
   }

   /**
    * To test the distributed cache sessions
    *
    * 1) Run mvn clean install
    *
    * 2) Open two terminals and run
    *
    * java -jar -Dserver.port=9000 infinispan-simple-tutorials-spring-session-1.0.0-SNAPSHOT.jar
    * java -jar -Dserver.port=9001 infinispan-simple-tutorials-spring-session-1.0.0-SNAPSHOT.jar
    *
    * The cluster should be form and the sessions are available under
    * http://localhost:9000/session and http://localhost:90001/session
    */
   public static void main(String[] args) {
      SpringApplication.run(SpringApp.class, args);
   }
}
