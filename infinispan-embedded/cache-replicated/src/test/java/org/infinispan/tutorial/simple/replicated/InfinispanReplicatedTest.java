package org.infinispan.tutorial.simple.replicated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanReplicatedTest {

    @BeforeAll
    public static void start() {
        InfinispanReplicated.createDefaultCacheManagerAndStartCache();
    }

    @AfterAll
    public static void stop() {
        InfinispanReplicated.stopDefaultCacheManager();
    }

    @Test
    public void testReplicatedCache() {
        assertNotNull(InfinispanReplicated.cacheManager);
        assertNotNull(InfinispanReplicated.cache);

        InfinispanReplicated.manipulateReplicatedCache();

        assertEquals(10, InfinispanReplicated.cache.size());
    }

}
