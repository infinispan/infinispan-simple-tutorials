package org.infinispan.tutorial.simple.ai.langchain4j;

import java.util.List;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;

public class InfinispanLangchain4j {

   static InfinispanEmbeddingStore embeddingStore;
   static EmbeddingModel embeddingModel;

   public static void main(String[] args) {
      initEmbeddingModel();
      connectAndCreateStore();
      try {
         storeAndSearch();
      } finally {
         disconnect();
      }
   }

   static void initEmbeddingModel() {
      embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
      System.out.println("Embedding model initialized. Dimension: " + embeddingModel.dimension());
   }

   static void connectAndCreateStore() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      embeddingStore = InfinispanEmbeddingStore.builder()
            .cacheName("langchain4j-embeddings")
            .dimension(embeddingModel.dimension())
            .infinispanConfigBuilder(builder)
            .distance(3)
            .build();
      System.out.println("Connected to Infinispan and created embedding store.");
   }

   static void storeAndSearch() {
      // Store some text segments with metadata
      addEmbedding("Infinispan is a distributed in-memory key/value data store",
            Metadata.from("source", "docs").put("topic", "overview"));
      addEmbedding("Infinispan supports vector search for AI use cases",
            Metadata.from("source", "docs").put("topic", "ai"));
      addEmbedding("Infinispan can be used as an embedding store with LangChain4j",
            Metadata.from("source", "tutorial").put("topic", "ai"));

      System.out.println("Stored 3 text segments with embeddings.\n");

      // Search by similarity
      String query = "How can I use Infinispan with AI?";
      System.out.println("Query: \"" + query + "\"");

      Embedding queryEmbedding = embeddingModel.embed(query).content();
      EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(3)
            .build();

      EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
      List<EmbeddingMatch<TextSegment>> matches = result.matches();

      System.out.println("Found " + matches.size() + " results:");
      for (EmbeddingMatch<TextSegment> match : matches) {
         System.out.printf("  Score: %.4f | Text: %s%n",
               match.score(), match.embedded().text());
      }
   }

   static void addEmbedding(String text, Metadata metadata) {
      TextSegment segment = TextSegment.from(text, metadata);
      Embedding embedding = embeddingModel.embed(segment).content();
      embeddingStore.add(embedding, segment);
   }

   static void disconnect() {
      if (embeddingStore != null) {
         embeddingStore.removeAll();
      }
   }
}
