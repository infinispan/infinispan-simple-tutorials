package org.infinispan.tutorial.simple.distributed;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.distributed.InfinispanDistributed.DIST_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfinispanDistributedTest {
    InfinispanDistributed infinispanDistributed = new InfinispanDistributed();

    @BeforeEach
    public void start() {
        infinispanDistributed.createDefaultCacheManager();
    }

    @AfterEach
    public void stop() {
        infinispanDistributed.stopDefaultCacheManager();
    }

    @Test
    public void testDistributed() {
        infinispanDistributed.createAndPopulateTheCache(10);
        assertEquals(10, infinispanDistributed.cm1.getCache(DIST_CACHE_NAME).size());
    }
}