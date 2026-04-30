package org.infinispan.tutorial.simple.query.programmatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class    InfinispanQueryProgrammaticIndexTest {

    @BeforeAll
    public static void init() {
        InfinispanQueryProgrammaticIndex.createCacheManagerAndCache();
    }

    @AfterAll
    public static void stop() {
        InfinispanQueryProgrammaticIndex.stopCacheManager();
    }

    @Test
    public void testQuery() {
        assertNotNull(InfinispanQueryProgrammaticIndex.cacheManager);
        assertNotNull(InfinispanQueryProgrammaticIndex.cache);

        List<Person> people = InfinispanQueryProgrammaticIndex.addDataAndPerformQuery();
        assertEquals(2, people.size());
    }
}
