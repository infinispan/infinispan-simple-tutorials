package org.infinispan.tutorial.simple.remote.percache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class InfinispanRemotePerCacheTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanRemotePerCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemotePerCache.disconnect(true);
    }

    @Test
    public void testRemotePerCacheConfiguration() {
        assertNotNull(InfinispanRemotePerCache.cacheManager);
        assertNull(InfinispanRemotePerCache.cache);
        assertNull(InfinispanRemotePerCache.anotherCache);
        assertNull(InfinispanRemotePerCache.uriCache);

        InfinispanRemotePerCache.manipulateCaches();
        assertNotNull(InfinispanRemotePerCache.cache);
        assertNotNull(InfinispanRemotePerCache.anotherCache);
        assertNotNull(InfinispanRemotePerCache.uriCache);
    }
}