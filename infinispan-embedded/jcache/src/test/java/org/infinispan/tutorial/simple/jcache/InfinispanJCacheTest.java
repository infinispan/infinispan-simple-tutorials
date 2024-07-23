package org.infinispan.tutorial.simple.jcache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanJCacheTest {

    @BeforeAll
    public static void start() {
        InfinispanJCache.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanJCache.close();
    }

    @Test
    public void testJCache() {
        assertNotNull(InfinispanJCache.jcacheProvider);
        assertNotNull(InfinispanJCache.cacheManager);
        assertNotNull(InfinispanJCache.cache);

        InfinispanJCache.manipulateCache();

        assertEquals("value", InfinispanJCache.cache.get("key"));
    }
}