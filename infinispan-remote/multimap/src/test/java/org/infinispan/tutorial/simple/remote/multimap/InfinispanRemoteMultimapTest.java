package org.infinispan.tutorial.simple.remote.multimap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanRemoteMultimapTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteMultimap.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteMultimap.disconnect(true);
    }

    @Test
    public void testRemoteMultimap() throws Exception {
        assertNotNull(InfinispanRemoteMultimap.cacheManager);
        assertNotNull(InfinispanRemoteMultimap.multimapCacheManager);
        assertNotNull(InfinispanRemoteMultimap.multimap);

        InfinispanRemoteMultimap.manipulateMultimap();
        Collection<String> people = InfinispanRemoteMultimap.multimap.get(2018).get(10, TimeUnit.SECONDS);
        assertEquals(1, people.size());
        assertEquals("Richard", people.toArray()[0]);
    }
}
