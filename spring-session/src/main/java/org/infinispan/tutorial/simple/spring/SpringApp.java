package org.infinispan.tutorial.simple.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.infinispan.spring.session.configuration.EnableInfinispanEmbeddedHttpSession;
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

   /**
    * Create Spring Cache Factory Bean.
    */
   @Bean
   public SpringEmbeddedCacheManagerFactoryBean springCache() {
      return new SpringEmbeddedCacheManagerFactoryBean();
   }

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

   public static void main(String[] args) {
      SpringApplication.run(SpringApp.class, args);
   }
}
