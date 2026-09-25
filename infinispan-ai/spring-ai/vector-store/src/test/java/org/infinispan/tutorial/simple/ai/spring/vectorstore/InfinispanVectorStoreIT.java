package org.infinispan.tutorial.simple.ai.spring.vectorstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class InfinispanVectorStoreIT {

   @Container
   static InfinispanContainer infinispan = new InfinispanContainer();

   @DynamicPropertySource
   static void infinispanProperties(DynamicPropertyRegistry registry) {
      registry.add("infinispan.remote.server-list",
            () -> infinispan.getHost() + ":" + infinispan.getMappedPort(11222));
   }

   @Autowired
   VectorStore vectorStore;

   @Test
   void addAndSearch() {
      List<Document> documents = List.of(
            new Document("Infinispan is a distributed in-memory key/value data store",
                  Map.of("source", "docs")),
            new Document("Infinispan supports vector search for AI use cases",
                  Map.of("source", "docs")),
            new Document("Spring AI provides a unified API for AI engineering",
                  Map.of("source", "spring")));

      vectorStore.add(documents);

      Awaitility.await().until(
            () -> vectorStore.similaritySearch(
                  SearchRequest.builder().query("vector search").topK(1).similarityThreshold(0).build()),
            hasSize(1));

      List<Document> results = vectorStore.similaritySearch(
            SearchRequest.builder().query("vector search AI").topK(2).similarityThreshold(0).build());

      assertThat(results).isNotEmpty();
      assertThat(results.get(0).getText()).contains("vector search");

      vectorStore.delete(documents.stream().map(Document::getId).toList());

      Awaitility.await().until(
            () -> vectorStore.similaritySearch(
                  SearchRequest.builder().query("vector search").topK(1).similarityThreshold(0).build()),
            hasSize(0));
   }
}
