package org.infinispan.tutorial.simple.redis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.redis.RedisClientCache.jedis;
import static org.infinispan.tutorial.simple.redis.RedisClientCache.manipulateWithRESP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RedisClientCacheTest {

    @BeforeAll
    public static void start() throws Exception {
        RedisClientCache.connect();
    }

    @AfterAll
    public static void stop() {
        RedisClientCache.disconnect();
    }

    @Test
    public void testRESPWithInfinispan() {
        assertNotNull(jedis);
        manipulateWithRESP();
        assertEquals("world", jedis.get("Hello"));
    }
}
