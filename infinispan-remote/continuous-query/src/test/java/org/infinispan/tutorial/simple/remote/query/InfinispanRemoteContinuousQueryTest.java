package org.infinispan.tutorial.simple.remote.query;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanRemoteContinuousQueryTest {

    @BeforeAll
    public static void start() throws Exception {
        InfinispanRemoteContinuousQuery.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteContinuousQuery.disconnect();
    }

    @Test
    public void testContinuousQuery() throws Exception {
        assertNotNull(InfinispanRemoteContinuousQuery.client);

        InfinispanRemoteContinuousQuery.createPostsAndQuery(1, false);

        assertEquals(1, InfinispanRemoteContinuousQuery.queryPosts.size());
        assertEquals(1, InfinispanRemoteContinuousQuery.client.getCache(TUTORIAL_CACHE_NAME).size());
    }

}