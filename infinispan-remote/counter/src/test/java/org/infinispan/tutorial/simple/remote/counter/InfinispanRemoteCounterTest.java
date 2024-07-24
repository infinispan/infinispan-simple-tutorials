package org.infinispan.tutorial.simple.remote.counter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InfinispanRemoteCounterTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteCounter.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteCounter.disconnect();
    }

    @Test
    public void testRemoteCounters() throws Exception {
        assertNotNull(InfinispanRemoteCounter.cacheManager);
        assertNotNull(InfinispanRemoteCounter.counterManager);

        InfinispanRemoteCounter.manipulateCounters();

        assertNotNull(InfinispanRemoteCounter.counter1);
        assertNotNull(InfinispanRemoteCounter.counter2);
        assertNotNull(InfinispanRemoteCounter.counter3);
        assertEquals(9, InfinispanRemoteCounter.counter1.sync().getValue());
        assertEquals(2, InfinispanRemoteCounter.counter2.sync().getValue());
        assertEquals(8, InfinispanRemoteCounter.counter3.sync().getValue());
    }
}
