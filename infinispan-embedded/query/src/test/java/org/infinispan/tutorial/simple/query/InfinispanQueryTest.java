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

        // Update by query: set surname to 'Bard' for all Williams
        int updated = InfinispanQuery.updateByQuery();
        assertEquals(2, updated);
        List<Person> bards = InfinispanQuery.cache
              .<Person>query("from org.infinispan.tutorial.simple.query.Person where surname = 'Bard'")
              .execute().list();
        assertEquals(2, bards.size());

        // Update by query with parameter: John -> Johnny
        int updatedWithParam = InfinispanQuery.updateByQueryWithParameter();
        assertEquals(1, updatedWithParam);
        List<Person> johnnies = InfinispanQuery.cache
              .<Person>query("from org.infinispan.tutorial.simple.query.Person where name = 'Johnny'")
              .execute().list();
        assertEquals(1, johnnies.size());
        assertEquals("Milton", johnnies.get(0).surname);
    }
}
