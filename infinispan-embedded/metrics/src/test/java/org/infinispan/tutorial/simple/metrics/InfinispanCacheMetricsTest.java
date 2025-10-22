package org.infinispan.tutorial.simple.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InfinispanCacheMetricsTest {

   InfinispanCacheMetrics metrics = new InfinispanCacheMetrics();

   @BeforeEach
   protected void start() {
      metrics.createDefaultCacheManager();
   }

   @AfterEach
   protected void stop() {
      metrics.stop();
   }

   @Test
   public void testInfinispanMetrics() throws Throwable {
      assertNotNull(metrics.dcm);

      metrics.createCacheWithMetricsAndPopulate(5);
      assertEquals(5, metrics.cache.size());
      assertEquals("1", metrics.extractRunningCachesNumber());
      assertTrue(metrics.scrapePrometheusMetrics().contains("vendor_cache_manager_DefaultCacheManager_cache_my_cache_statistics_approximate_entries"));
   }
}
