package org.infinispan.tutorial.simple.remote.listen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanRemoteListenTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteListen.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteListen.disconnect(true);
    }

    @Test
    public void testRemoteListen() throws Exception {
        assertNotNull(InfinispanRemoteListen.cacheManager);
        assertNotNull(InfinispanRemoteListen.cache);

        InfinispanRemoteListen.registerListener();

        assertNotNull(InfinispanRemoteListen.listener);

        InfinispanRemoteListen.manipulateCache();

        Thread.sleep(1000);

        String logTrack = InfinispanRemoteListen.listener.logTrack.toString();
        assertEquals(2, InfinispanRemoteListen.cache.size());
        assertTrue(logTrack.contains("Created"));
        assertTrue(logTrack.contains("About to modify"));
    }
}
