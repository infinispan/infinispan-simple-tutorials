package tutorial.spring.infinispan;

import static org.assertj.core.api.Assertions.assertThat;

import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CharactersResourceIT {

   @Container
   static InfinispanContainer infinispan = new InfinispanContainer("quay.io/infinispan-test/server:main");

   @DynamicPropertySource
   static void infinispanProperties(DynamicPropertyRegistry registry) {
      registry.add("infinispan.remote.connection-uri", infinispan::getConnectionURI);
   }

   @LocalServerPort
   int port;

   @Test
   void testGetCharacterById() {
      RestClient client = RestClient.create("http://localhost:" + port);
      String body = client.get().uri("/characters/1").retrieve().body(String.class);
      assertThat(body).isNotNull();
   }

   @Test
   void testSearchCharacter() {
      RestClient client = RestClient.create("http://localhost:" + port);
      String body = client.get().uri("/characters/query?term=Felix").retrieve().body(String.class);
      assertThat(body).contains("Felix");
   }
}
