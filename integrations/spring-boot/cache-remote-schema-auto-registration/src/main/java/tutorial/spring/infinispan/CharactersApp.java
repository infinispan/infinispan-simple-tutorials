package tutorial.spring.infinispan;

import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CharactersApp {

   public static void main(String[] args) {
      TutorialsConnectorHelper.startInfinispanContainer();
      if (TutorialsConnectorHelper.isContainerStarted()) {
         String connectionUri = String.format("hotrod://%s:%s@127.0.0.1:%d",
               TutorialsConnectorHelper.USER,
               TutorialsConnectorHelper.PASSWORD,
               TutorialsConnectorHelper.INFINISPAN_CONTAINER.getFirstMappedPort());
         SpringApplication.run(CharactersApp.class,
               "--infinispan.remote.connection-uri=" + connectionUri);
      } else {
         SpringApplication.run(CharactersApp.class, args);
      }
   }
}
