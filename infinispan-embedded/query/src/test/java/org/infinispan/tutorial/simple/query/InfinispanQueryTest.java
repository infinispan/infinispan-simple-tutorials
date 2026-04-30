package org.infinispan.tutorial.simple.query;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanQueryTest {

    @BeforeAll
    public static void init() {
        InfinispanQuery.createCacheManagerAndCache();
    }

    @AfterAll
    public static void stop() {
        InfinispanQuery.stopCacheManager();
    }

    @Test
    public void testQuery() {
        assertNotNull(InfinispanQuery.cacheManager);
        assertNotNull(InfinispanQuery.cache);

        List<Person> people = InfinispanQuery.addDataAndPerformQuery();
        assertEquals(2, people.size());
    }
}
