package org.infinispan.tutorial.simple.ai.quarkus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InfinispanAssistantResourceTest {

   @Test
   void testChatEndpoint() {
      given()
            .queryParam("question", "What is Infinispan?")
            .when()
            .get("/chat")
            .then()
            .statusCode(200)
            .body(notNullValue());
   }

   @Test
   void testChatEndpointWithCacheModeQuestion() {
      given()
            .queryParam("question", "What cache modes does Infinispan support?")
            .when()
            .get("/chat")
            .then()
            .statusCode(200)
            .body(notNullValue());
   }
}
