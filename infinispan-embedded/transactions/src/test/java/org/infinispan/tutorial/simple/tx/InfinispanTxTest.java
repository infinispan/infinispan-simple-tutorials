package org.infinispan.tutorial.simple.tx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InfinispanTxTest {

    InfinispanTx infinispanTx = new InfinispanTx();

    @BeforeEach
    public void start() {
        infinispanTx.createDefaultCacheManagerAndInitCache();
    }

    @AfterEach
    public void stop() {
        infinispanTx.stopDefaultCacheManager();
    }

    @Test
    public void testTransactionalCache() throws Exception {
        infinispanTx.putAndCommit(Map.of("key", "value"));
        assertEquals(1, infinispanTx.cache.size());
        assertEquals("value", infinispanTx.cache.get("key"));
        infinispanTx.putAndRollback(Map.of("key", "another", "hello", "world"));
        assertEquals("value", infinispanTx.cache.get("key"));
        assertNull(infinispanTx.cache.get("hello"));
    }
}