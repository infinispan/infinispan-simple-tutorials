package org.infinispan.tutorial.simple.remote.marshalling;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanProgrammaticProtostreamMarshallingTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanProgrammaticProtostreamMarshalling.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanProgrammaticProtostreamMarshalling.disconnect(true);
    }

    @Test
    public void testMarshalling() throws Exception {
        assertNotNull(InfinispanProgrammaticProtostreamMarshalling.client);
        assertNotNull(InfinispanProgrammaticProtostreamMarshalling.magazineRemoteCache);

        assertEquals(0, InfinispanProgrammaticProtostreamMarshalling.magazineRemoteCache.size());
        InfinispanProgrammaticProtostreamMarshalling.manipulateCache();
        assertEquals(3, InfinispanProgrammaticProtostreamMarshalling.magazineRemoteCache.size());
    }
}
