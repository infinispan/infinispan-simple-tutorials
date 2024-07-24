package org.infinispan.tutorial.simple.remote.xsite;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InfinispanRemoteSwitchClusterTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteSwitchCluster.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteSwitchCluster.disconnect(true);
    }

    @Test
    public void testXSite() {
        assertNotNull(InfinispanRemoteSwitchCluster.XSITE_CACHE);

        InfinispanRemoteSwitchCluster.manipulateCacheAndSwitchCluster();

        assertNotNull(InfinispanRemoteSwitchCluster.log);
        String log = InfinispanRemoteSwitchCluster.log.toString();
        assertTrue(log.contains("LON members"));
        assertTrue(log.contains("11222"));
        assertTrue(log.contains("NYC members"));
        assertTrue(log.contains("31223"));
    }
}
