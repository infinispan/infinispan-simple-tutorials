package org.infinispan.tutorial.simple.clusterexec;


import org.infinispan.remoting.transport.Address;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanClusterExecTest {

    InfinispanClusterExec infinispanClusterExec = new InfinispanClusterExec();

    @BeforeEach
    public void start() {
        infinispanClusterExec.createCacheManager();
    }

    @AfterEach
    public void stop() {
        infinispanClusterExec.stopDefaultCacheManager();
    }

    @Test
    public void testClusteredExec() throws Exception {
        UUID uuid = UUID.randomUUID();
        Map<Object, Address> values = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        infinispanClusterExec.submitTask(cm -> uuid.toString(),
                (address, result, exception) -> {
                    values.put(result, address);
                    countDownLatch.countDown();
                });
        countDownLatch.await(10, TimeUnit.SECONDS);
        assertEquals(1, values.size());
        assertNotNull(values.get(uuid.toString()));
    }
}