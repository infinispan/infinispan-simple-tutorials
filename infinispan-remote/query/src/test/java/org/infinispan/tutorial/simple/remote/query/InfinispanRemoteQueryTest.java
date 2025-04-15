package org.infinispan.tutorial.simple.remote.query;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InfinispanRemoteQueryTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanRemoteQuery.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteQuery.disconnect(true);
    }

    @Test
    public void testRemoteQuery() {
        assertNotNull(InfinispanRemoteQuery.client);
        assertNotNull(InfinispanRemoteQuery.peopleCache);
        assertNotNull(InfinispanRemoteQuery.teamCache);
        InfinispanRemoteQuery.peopleCache.clear();
        InfinispanRemoteQuery.teamCache.clear();

        List<Person> people = InfinispanRemoteQuery.queryAllPeople();
        assertEquals(0, people.size());

        InfinispanRemoteQuery.addDataToPeopleCache();

        people = InfinispanRemoteQuery.queryAllPeople();
        assertEquals(4, people.size());

        List<Person> peopleFiltered = InfinispanRemoteQuery.queryWithWhereStatementOnValues();
        assertEquals(1, peopleFiltered.size());
        assertEquals("Granger", peopleFiltered.get(0).lastName());

        List<Person> peopleFilteredByKey = InfinispanRemoteQuery.queryByKey();
        assertEquals(1, peopleFilteredByKey.size());
        assertEquals("Malfoy", peopleFilteredByKey.get(0).lastName());

        List<InfinispanRemoteQuery.PersonDTO> personDTOS = InfinispanRemoteQuery.queryWithProjection();
        assertEquals(3, personDTOS.size());
        Set<String> collected = personDTOS.stream().map(InfinispanRemoteQuery.PersonDTO::pseudo).collect(Collectors.toSet());
        assertEquals(3, collected.size());
        assertTrue(collected.contains("dmalfoy"));

        // Shut up Malfoy !!
        InfinispanRemoteQuery.deleteByQuery();

        // Malfoy has been removed
        peopleFilteredByKey = InfinispanRemoteQuery.queryByKey();
        assertEquals(0, peopleFilteredByKey.size());

        // Count teams before adding values
        assertEquals(0, InfinispanRemoteQuery.countAllTeam());
        InfinispanRemoteQuery.addDataToTeamCache();
        assertEquals(3, InfinispanRemoteQuery.countAllTeam());
        InfinispanRemoteQuery.queryWithScoreAndFilterOnNestedValues();
    }
}