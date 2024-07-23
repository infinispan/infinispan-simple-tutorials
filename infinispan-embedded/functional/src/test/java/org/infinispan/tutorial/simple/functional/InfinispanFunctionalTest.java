package org.infinispan.tutorial.simple.functional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InfinispanFunctionalTest {

    @BeforeAll
    public static void start() {
        InfinispanFunctional.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanFunctional.stop();
    }

    @Test
    public void testFunctionalMap() throws Exception {
        assertNotNull(InfinispanFunctional.cacheManager);
        assertNotNull(InfinispanFunctional.cache);

        InfinispanFunctional.manipulateFunctionalMap();

        assertEquals(2, InfinispanFunctional.cache.size());
    }
}