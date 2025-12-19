package org.infinispan.tutorial.simple.spring.remote;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.infinispan.tutorial.simple.spring.remote.Data.BASQUE_NAMES_CACHE;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class BasqueNamesIT {

   public static final String ID = "test-Ane";

   @Container
   static InfinispanContainer infinispan = new InfinispanContainer();

   @DynamicPropertySource
   static void infinispanProperties(DynamicPropertyRegistry registry) {
      registry.add(
            "infinispan.remote.server-list",
            () -> infinispan.getHost() + ":" + infinispan.getMappedPort(11222)
      );
   }

   @Autowired
   BasqueNamesRepository repository;

   @Autowired
   RemoteCacheManager remoteCacheManager;

   @Test
   void findById_usesRemoteCache() {
      assertThat(remoteCacheManager).isNotNull();
      RemoteCache<String, BasqueName> cache = remoteCacheManager.getCache(BASQUE_NAMES_CACHE);
      assertThat(cache).isNotNull();
      // Ane does not exist
      assertThat(cache.containsKey(ID)).isFalse();

      // Create in the database
      repository.create(ID, "Ane");
      // Ane does not exist
      assertThat(cache.containsKey(ID)).isFalse();

      // first call → hits "database"
      BasqueName first = repository.findById(ID);

      // Ane does exist
      assertThat(cache.containsKey(ID)).isTrue();

      // second call → must be served from Infinispan
      BasqueName second = repository.findById(ID);

      assertThat(first).isNotNull();
      assertThat(second).isEqualTo(first);
   }

}
