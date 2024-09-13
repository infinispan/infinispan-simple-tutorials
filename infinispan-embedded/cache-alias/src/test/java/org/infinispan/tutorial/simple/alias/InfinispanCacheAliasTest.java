package org.infinispan.tutorial.simple.alias;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.infinispan.tutorial.simple.alias.InfinispanCacheAlias.ALIAS_1;
import static org.infinispan.tutorial.simple.alias.InfinispanCacheAlias.ALIAS_2;
import static org.infinispan.tutorial.simple.alias.InfinispanCacheAlias.DIST_CACHE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InfinispanCacheAliasTest {
    InfinispanCacheAlias infinispanCacheAlias = new InfinispanCacheAlias();

    @BeforeEach
    public void start() {
        infinispanCacheAlias.createDefaultCacheManager();
    }

    @AfterEach
    public void stop() {
        infinispanCacheAlias.stopDefaultCacheManager();
    }

    @Test
    public void testCacheAlias() {
        assertNotNull(infinispanCacheAlias.cm1);
        infinispanCacheAlias.createACacheWithAliasAndPopulate(1);
        assertEquals(1, infinispanCacheAlias.cm1.getCache(DIST_CACHE_NAME).size());
        assertEquals(1, infinispanCacheAlias.cm1.getCache(ALIAS_1).size());
        assertNull(infinispanCacheAlias.cm1.getCache(ALIAS_2));

        infinispanCacheAlias.updateCacheConfigWithSecondAlias();

        assertEquals(1, infinispanCacheAlias.cm1.getCache(ALIAS_2).size());
    }
}