package org.infinispan.tutorial.simple.multimap;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanMultimapTest {

    @BeforeAll
    public static void start() {
        InfinispanMultimap.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanMultimap.stop();
    }

    @Test
    public void testMultimap() throws Exception {
        assertNotNull(InfinispanMultimap.multimap);
        InfinispanMultimap.manipulateMultimap();
        assertEquals(3, InfinispanMultimap.multimap.size().get(10, TimeUnit.SECONDS));
    }
}