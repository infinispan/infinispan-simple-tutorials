package org.infinispan.tutorial.simple.listen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanListenTest {

    @BeforeAll
    public static void start() {
        InfinispanListen.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanListen.stop();
    }

    @Test
    public void testListener() {
        assertNotNull(InfinispanListen.cacheManager);
        assertNotNull(InfinispanListen.cache);
        assertNotNull(InfinispanListen.listener);

        InfinispanListen.manipulateCache();

        assertEquals(2, InfinispanListen.cache.size());
        assertEquals(2, InfinispanListen.listener.created.size());
        assertEquals(1, InfinispanListen.listener.updated.size());
    }
}