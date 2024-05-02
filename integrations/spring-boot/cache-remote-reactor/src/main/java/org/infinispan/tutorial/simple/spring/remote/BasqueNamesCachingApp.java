package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BasqueNamesCachingApp {

   public static void main(String... args) {
      TutorialsConnectorHelper.startInfinispanContainer();
      SpringApplication.run(BasqueNamesCachingApp.class);
   }
}
