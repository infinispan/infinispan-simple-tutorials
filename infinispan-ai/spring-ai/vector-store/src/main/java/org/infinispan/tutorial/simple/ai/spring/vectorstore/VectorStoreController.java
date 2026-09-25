package org.infinispan.tutorial.simple.ai.spring.vectorstore;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VectorStoreController {

   private final VectorStore vectorStore;

   public VectorStoreController(VectorStore vectorStore) {
      this.vectorStore = vectorStore;
   }

   // tag::load[]
   @PostMapping(path = "/load")
   public String loadData() {
      List<Document> documents = List.of(
            new Document("Infinispan is a distributed in-memory key/value data store",
                  Map.of("source", "docs", "topic", "overview")),
            new Document("Infinispan supports vector search for AI use cases",
                  Map.of("source", "docs", "topic", "ai")),
            new Document("Spring AI provides a unified API for AI engineering",
                  Map.of("source", "spring", "topic", "ai")),
            new Document("Infinispan can be used as a vector store with Spring AI",
                  Map.of("source", "tutorial", "topic", "integration")));

      vectorStore.add(documents);
      return "Loaded " + documents.size() + " documents";
   }
   // end::load[]

   // tag::search[]
   @GetMapping(path = "/search")
   public List<Document> search(@RequestParam(defaultValue = "How can I use Infinispan with AI?") String query) {
      return vectorStore.similaritySearch(
            SearchRequest.builder()
                  .query(query)
                  .topK(3)
                  .similarityThreshold(0)
                  .build());
   }
   // end::search[]
}
