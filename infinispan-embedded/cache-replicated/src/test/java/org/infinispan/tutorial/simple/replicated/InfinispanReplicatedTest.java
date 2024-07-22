package org.infinispan.tutorial.simple.replicated;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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