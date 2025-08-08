package org.infinispan.tutorial;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class CharactersResourceTest {

    @Test
    void testGetCharacterById_Success() {
        String existingCharacterId = "1";

        given()
                .pathParam("id", existingCharacterId)
                .when()
                .get("/characters/{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(notNullValue());
    }

    @Test
    void testGetCharacterById_NotFound() {
        // Use an ID that definitely doesn't exist
        String nonExistentId = "999999";

        given()
                .pathParam("id", nonExistentId)
                .when()
                .get("/characters/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void testSearchCharacter_WithValidTerm() {
        String searchTerm = "Felix";

        given()
                .queryParam("term", searchTerm)
                .when()
                .get("/characters/query")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("size()", greaterThan(0));
    }
}