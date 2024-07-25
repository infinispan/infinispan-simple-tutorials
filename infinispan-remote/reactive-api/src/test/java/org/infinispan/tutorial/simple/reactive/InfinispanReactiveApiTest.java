package org.infinispan.tutorial.simple.reactive;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanReactiveApiTest {

    @BeforeAll
    public static void start() {
        InfinispanReactiveApi.connect();
    }

    @AfterAll
    public static void stop() {
        InfinispanReactiveApi.disconnect(true);
    }

    @Test
    public void testReactive() {
        assertNotNull(InfinispanReactiveApi.infinispan);
        InfinispanReactiveApi.initCache();
        assertNotNull(InfinispanReactiveApi.cache);
        InfinispanReactiveApi.manipulateCacheReactive();
        assertEquals("reactive", InfinispanReactiveApi
                .infinispan.sync().caches()
                .get(TUTORIAL_CACHE_NAME).get("hello"));
    }

}
