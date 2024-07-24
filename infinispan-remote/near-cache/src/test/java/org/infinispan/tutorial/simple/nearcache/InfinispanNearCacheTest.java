package org.infinispan.tutorial.simple.nearcache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanNearCacheTest {

    @BeforeAll
    public static void start() {
        InfinispanNearCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanNearCache.disconnect(true);
    }

    @Test
    public void testNearCache() {
        assertNotNull(InfinispanNearCache.testCache);
        assertNotNull(InfinispanNearCache.withNearCaching);
    }

}