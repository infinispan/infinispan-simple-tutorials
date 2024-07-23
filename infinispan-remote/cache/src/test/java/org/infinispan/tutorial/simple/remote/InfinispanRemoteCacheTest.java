package org.infinispan.tutorial.simple.remote;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanRemoteCacheTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteCache.deconnect();
    }

    @Test
    public void testRemoteCache() {
        assertNotNull(InfinispanRemoteCache.cache);

        InfinispanRemoteCache.manipulateCache();

        assertEquals("value", InfinispanRemoteCache.cache.get("key"));
    }

}
