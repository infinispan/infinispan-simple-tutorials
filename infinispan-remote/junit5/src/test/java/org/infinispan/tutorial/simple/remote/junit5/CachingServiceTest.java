package org.infinispan.tutorial.simple.remote.junit5;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.server.test.junit5.InfinispanServerExtension;
import org.infinispan.server.test.junit5.InfinispanServerExtensionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test is a JUnit 5 test that uses TestContainers under the hood
 */
public class CachingServiceTest {

   @RegisterExtension
   static InfinispanServerExtension infinispanServerExtension = InfinispanServerExtensionBuilder.server();

   @Test
   public void testUsingRemoteCacheManager(){
      RemoteCacheManager remoteCacheManager = infinispanServerExtension.hotrod().createRemoteCacheManager();

      // Create the CachingService passing the RemoteCacheManager
      CachingService cachingService = new CachingService(remoteCacheManager);

      // Use the service to store a name
      cachingService.storeName("123", "Mickey");

      // Assert values
      assertTrue(cachingService.exists("Mickey"));
      assertFalse(cachingService.exists("Donald"));
   }

   @Test
   public void testUsingACache(){
      // Grab the cache created in the context of this test
      RemoteCache<String, String> cache = infinispanServerExtension.hotrod().create();

      // Put some data in the cache
      cache.put("123", "Mickey");
      cache.put("456", "Donald");

      // Create the CachingService, using the cache created in the cycle of this test
      CachingService cachingService = new CachingService(cache);

      // Assert Values
      assertTrue(cachingService.exists("Mickey"));
      assertTrue(cachingService.exists("Donald"));
      assertFalse(cachingService.exists("Minie"));
   }
}
