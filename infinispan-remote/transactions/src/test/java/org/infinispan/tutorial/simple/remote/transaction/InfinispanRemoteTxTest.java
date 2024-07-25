package org.infinispan.tutorial.simple.remote.transaction;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.remote.transaction.InfinispanRemoteTx.cache;
import static org.infinispan.tutorial.simple.remote.transaction.InfinispanRemoteTx.cacheManager;
import static org.infinispan.tutorial.simple.remote.transaction.InfinispanRemoteTx.connectToInfinispan;
import static org.infinispan.tutorial.simple.remote.transaction.InfinispanRemoteTx.disconnect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanRemoteTxTest {

    @BeforeAll
    public static void start() throws Exception {
        connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        disconnect(true);
    }

    @Test
    public void testTransactions() throws Exception {
        assertNotNull(cacheManager);
        assertNotNull(cache);

        InfinispanRemoteTx.manipulateWithTx();

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
    }

}
