package org.infinispan.tutorial.simple.streams;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfinispanStreamsTest {

    InfinispanStreams infinispanStreams = new InfinispanStreams();

    @BeforeEach
    public void start() {
        infinispanStreams.createDefaultCacheManagerAndInitCache();
    }

    @AfterEach
    public void stop() {
        infinispanStreams.stopDefaultCacheManager();
    }

    @Test
    public void testStreamsCache() {
        infinispanStreams.storeKeyValues(20);
        assertEquals(20, infinispanStreams.cache.size());
        int result = infinispanStreams.mapAndReduceKeys();
        assertEquals(190, result);
    }
}