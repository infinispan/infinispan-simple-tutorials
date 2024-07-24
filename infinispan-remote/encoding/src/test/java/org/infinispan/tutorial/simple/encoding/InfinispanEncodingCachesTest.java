package org.infinispan.tutorial.simple.encoding;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanEncodingCachesTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanEncodingCaches.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanEncodingCaches.disconnect(true);
    }

    @Test
    public void testEncoding() {
        assertNotNull(InfinispanEncodingCaches.cacheManager);
        assertNotNull(InfinispanEncodingCaches.xmlCache);
        assertNotNull(InfinispanEncodingCaches.textCache);
        assertNotNull(InfinispanEncodingCaches.jsonCache);

        InfinispanEncodingCaches.manipulateCachesAndPrint();

        assertEquals("<name>infinispan</name>", InfinispanEncodingCaches.xmlCache.get("xml"));
        assertEquals("{\"name\": \"infinispan\"}", InfinispanEncodingCaches.jsonCache.get("\"json\""));
        assertEquals("诶, 你好.", InfinispanEncodingCaches.textCache.get("text"));
    }
}
