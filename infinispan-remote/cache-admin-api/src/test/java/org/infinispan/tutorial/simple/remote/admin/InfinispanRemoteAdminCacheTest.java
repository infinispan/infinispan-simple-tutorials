package org.infinispan.tutorial.simple.remote.admin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.infinispan.tutorial.simple.remote.admin.InfinispanRemoteAdminCache.CACHE_WITH_TEMPLATE;
import static org.infinispan.tutorial.simple.remote.admin.InfinispanRemoteAdminCache.CACHE_WITH_XMLCONFIGURATION;
import static org.infinispan.tutorial.simple.remote.admin.InfinispanRemoteAdminCache.SIMPLE_CACHE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InfinispanRemoteAdminCacheTest {

    @BeforeAll
    public static void start() {
        InfinispanRemoteAdminCache.connectToInfinispan();
    }

    @AfterAll
    public static void stop() {
        InfinispanRemoteAdminCache.disconnect();
    }

    @Test
    public void testCacheManager() {
        assertNotNull(InfinispanRemoteAdminCache.cacheManager);
    }

    @Test
    public void testCreateSimpleCache() {
        InfinispanRemoteAdminCache.createSimpleCache();
        assertNotNull(InfinispanRemoteAdminCache.cacheManager.getCache(SIMPLE_CACHE));
    }

    @Test
    public void testCreateWithTemplate() throws IOException {
        InfinispanRemoteAdminCache.cacheWithTemplate();
        assertNotNull(InfinispanRemoteAdminCache.cacheManager.getCache(CACHE_WITH_TEMPLATE));
    }

    @Test
    public void createCacheWithXMLConfiguration() throws IOException {
        InfinispanRemoteAdminCache.createCacheWithXMLConfiguration();
        assertNotNull(InfinispanRemoteAdminCache.cacheManager.getCache(CACHE_WITH_XMLCONFIGURATION));
    }
}