package org.infinispan.tutorial.simple.remote;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanAuthorizationCacheTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanAuthorizationCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanAuthorizationCache.disconnect(true);
    }

    @Test
    public void testInfinispanAuthorizationCache() {
        assertNotNull(InfinispanAuthorizationCache.cacheManager);
        assertNotNull(InfinispanAuthorizationCache.cache);
        assertNotNull(InfinispanAuthorizationCache.securedCache);

        InfinispanAuthorizationCache.manipulateCache();

        assertTrue(InfinispanAuthorizationCache.message.contains("unauthorized"));
    }

}
