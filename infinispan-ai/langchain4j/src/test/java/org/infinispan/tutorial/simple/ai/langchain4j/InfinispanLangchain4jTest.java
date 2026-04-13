package org.infinispan.tutorial.simple.ai.langchain4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanLangchain4jTest {

   @BeforeAll
   public static void start() {
      InfinispanLangchain4j.initEmbeddingModel();
      InfinispanLangchain4j.connectAndCreateStore();
   }

   @AfterAll
   public static void stop() {
      InfinispanLangchain4j.disconnect();
   }

   @Test
   public void testStoreAndSearch() {
      assertNotNull(InfinispanLangchain4j.embeddingStore);
      assertNotNull(InfinispanLangchain4j.embeddingModel);
      InfinispanLangchain4j.storeAndSearch();
   }
}
