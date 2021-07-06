package org.infinispan.tutorial.simple.spring.embedded;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BasqueNamesCachingApp {

   public static void main(String[] args) {
      new SpringApplicationBuilder().sources(BasqueNamesCachingApp.class).run(args);
   }
}
