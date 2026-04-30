package org.infinispan.tutorial.simple.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanMapTest {

    @BeforeAll
    public static void start() {
        InfinispanMap.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanMap.stop();
    }

    @Test
    public void testMap() {
        assertNotNull(InfinispanMap.cache);

        InfinispanMap.manipulateLocalCache();

        assertEquals(1, InfinispanMap.cache.size());
    }
}
