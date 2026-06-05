package org.infinispan.tutorial.simple.alias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanCacheAliasTest {

    @BeforeAll
    public static void start() {
        InfinispanCacheAlias.connectAndStore();
    }

    @AfterAll
    public static void stop() {
        InfinispanCacheAlias.disconnect();
    }

    @Test
    public void testCacheAlias() {
        assertNotNull(InfinispanCacheAlias.cache);
        assertNull(InfinispanCacheAlias.cacheAlias);

        InfinispanCacheAlias.addAlias();
        assertEquals("value", InfinispanCacheAlias.cache.get("key"));

        InfinispanCacheAlias.addAlias();
        assertNotNull(InfinispanCacheAlias.cacheAlias);
        assertEquals("value", InfinispanCacheAlias.cacheAlias.get("key"));
    }

}
