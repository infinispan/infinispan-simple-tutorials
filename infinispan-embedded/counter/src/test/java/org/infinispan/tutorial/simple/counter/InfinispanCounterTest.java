package org.infinispan.tutorial.simple.counter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanCounterTest {

    InfinispanCounter infinispanCounter = new InfinispanCounter();

    @BeforeEach
    public void start() {
        infinispanCounter.createCounterManager();
    }

    @AfterEach
    public void stop() {
        infinispanCounter.stopCounterManager();
    }

    @Test
    public void testInfinispanCounter() throws Exception {
        infinispanCounter.createAndManipulateCounters();

        assertNotNull(InfinispanCounter.counter1);
        assertEquals(9, InfinispanCounter.counter1.sync().getValue());
        assertNotNull(InfinispanCounter.counter2);
        assertEquals(2, InfinispanCounter.counter2.sync().getValue());
        assertNotNull(InfinispanCounter.counter3);
        assertEquals(8, InfinispanCounter.counter3.sync().getValue());
    }
}