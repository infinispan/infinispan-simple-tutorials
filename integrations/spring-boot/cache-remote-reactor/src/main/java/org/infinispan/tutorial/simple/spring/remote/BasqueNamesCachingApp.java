package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BasqueNamesCachingApp {

   private static ConfigurableApplicationContext ctx;

   public static void main(String... args) {
      TutorialsConnectorHelper.startInfinispanContainer();
      if (TutorialsConnectorHelper.isContainerStarted()) {
         String[] actualArgs = new String[]{"infinispan.remote.server-list=127.0.0.1:" + TutorialsConnectorHelper.INFINISPAN_CONTAINER.getFirstMappedPort()};
         ctx = SpringApplication.run(BasqueNamesCachingApp.class, actualArgs);
      } else {
         ctx = SpringApplication.run(BasqueNamesCachingApp.class, args);
      }
   }

   public static void exitApplication() {
      int staticExitCode = SpringApplication.exit(ctx, new ExitCodeGenerator() {

         @Override
         public int getExitCode() {
            TutorialsConnectorHelper.stopInfinispanContainer();
            // no errors
            return 0;
         }
      });

      System.out.println("Exit");
      System.exit(staticExitCode);
   }
}
