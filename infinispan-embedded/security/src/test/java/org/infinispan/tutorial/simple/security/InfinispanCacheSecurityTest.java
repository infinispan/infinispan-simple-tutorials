package org.infinispan.tutorial.simple.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InfinispanCacheSecurityTest {

   InfinispanCacheSecurity security = new InfinispanCacheSecurity();

   @BeforeEach
   protected void start() {
      security.createDefaultCacheManager();
   }

   @AfterEach
   protected void stop() {
      security.stop();
   }

   @Test
   public void testCacheSecurity() {
      assertNotNull(security.dcm);

      security.createAllCachesAndPopulate(5);

      assertEquals(5, security.getCacheSize(InfinispanCacheSecurity.READ_ONLY_USER, security.cache));
      assertEquals(5, security.getCacheSize(InfinispanCacheSecurity.SECRET_USER, security.secretCache));

      // Only the secret user can access the secret cache.
      assertThrows(SecurityException.class, () -> security.getCacheSize(InfinispanCacheSecurity.READ_ONLY_USER, security.secretCache));

      // Write-only user can not perform bulk read operations.
      assertThrows(SecurityException.class, () -> security.getCacheSize(InfinispanCacheSecurity.WRITE_ONLY_USER, security.cache));
   }
}
