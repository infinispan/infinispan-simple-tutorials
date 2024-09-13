package org.infinispan.tutorial.simple.alias;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanCacheAliasTest {

    @BeforeAll
    public static void start() {
        InfinispanCacheAlias.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanCacheAlias.disconnect();
    }

    @Test
    public void testCacheAlias() {
        assertNotNull(InfinispanCacheAlias.cache);
        assertNull(InfinispanCacheAlias.cacheAlias);

        InfinispanCacheAlias.manipulateCache();
        assertEquals("value", InfinispanCacheAlias.cache.get("key"));

        InfinispanCacheAlias.addAlias();
        assertNotNull(InfinispanCacheAlias.cacheAlias);
        assertEquals("value", InfinispanCacheAlias.cacheAlias.get("key"));
    }

}
