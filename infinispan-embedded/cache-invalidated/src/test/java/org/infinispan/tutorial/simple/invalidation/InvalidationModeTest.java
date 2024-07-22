package org.infinispan.tutorial.simple.invalidation;


import org.infinispan.Cache;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvalidationModeTest {

    @BeforeAll
    public static void init() {
        InvalidationMode.createCacheManagerAndInitCache();
    }

    @AfterAll
    public static void stop() {
        InvalidationMode.stopCacheManager();
    }

    @Test
    public void testInvalidationMode() {
        Assertions.assertNotNull(InvalidationMode.cacheManager);
        Assertions.assertNotNull(InvalidationMode.cache);

        Cache<String, String> cache = InvalidationMode.cache;
        cache.putForExternalRead("foo", "bar");
        assertEquals(1, cache.size());
        assertEquals("bar", cache.get("foo"));
    }
}