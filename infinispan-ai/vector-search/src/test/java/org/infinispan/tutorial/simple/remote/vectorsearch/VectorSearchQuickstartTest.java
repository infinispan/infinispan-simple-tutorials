package org.infinispan.tutorial.simple.remote.vectorsearch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VectorSearchQuickstartTest {

   @BeforeAll
   public static void start() throws Exception {
      VectorSearchQuickstart.connect();
      VectorSearchQuickstart.populateBeers();
   }

   @AfterAll
   public static void stop() {
      VectorSearchQuickstart.disconnect();
   }

   @Test
   public void testVectorSearch() {
      assertNotNull(VectorSearchQuickstart.cacheManager);
      assertNotNull(VectorSearchQuickstart.cache);
      VectorSearchQuickstart.fullTextSearch();
      VectorSearchQuickstart.keywordAndRangeFilter();
      VectorSearchQuickstart.projectionsAndSorting();
      VectorSearchQuickstart.knnVectorSearch();
      VectorSearchQuickstart.scoreProjection();
      VectorSearchQuickstart.hybridFilterByStyle();
      VectorSearchQuickstart.hybridFilterByCountry();
      VectorSearchQuickstart.hybridFullTextAndVector();
   }
}
