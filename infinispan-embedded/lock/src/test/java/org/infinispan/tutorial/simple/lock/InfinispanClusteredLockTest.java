package org.infinispan.tutorial.simple.lock;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanClusteredLockTest {

    @BeforeAll
    public static void start() {
        InfinispanClusteredLock.createAndStartComponents();
    }

    @AfterAll
    public static void stop() {
        InfinispanClusteredLock.stop();
    }

    @Test
    public void testClusteredLock() throws Exception {
        assertNotNull(InfinispanClusteredLock.cacheManager);
        assertNotNull(InfinispanClusteredLock.clusteredLockManager);
        assertNotNull(InfinispanClusteredLock.lock);
        assertNotNull(InfinispanClusteredLock.counter);

        InfinispanClusteredLock.changeCounterWithLocks();

        assertEquals(3, InfinispanClusteredLock.counter.get());
    }

}