package org.infinispan.tutorial.simple.remote.multimap;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.multimap.MultimapCacheManager;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCache;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCacheManagerFactory;
import org.infinispan.commons.api.CacheContainerAdmin;

/**
 * The Remote Multimap simple tutorial.
 * <p>
 * The remote multimap are available in Infinispan since version 9.2
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class InfinispanRemoteMultimap {

   private static final String PEOPLE_MULTIMAP = "people-multimap";

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1")
            .port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
      // Connect to the server and create a cache
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Create people cache if needed with an existing template name
      cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache(PEOPLE_MULTIMAP, "org.infinispan.DIST_SYNC");

      // Retrieve the MultimapCacheManager from the CacheManager.
      MultimapCacheManager multimapCacheManager = RemoteMultimapCacheManagerFactory.from(cacheManager);

      // Retrieve the multimap cache.
      RemoteMultimapCache<Integer, String> people = multimapCacheManager.get(PEOPLE_MULTIMAP);
      people.put(2016, "Alberto");
      people.put(2016, "Oihana");
      people.put(2016, "Roman");
      people.put(2016, "Ane");
      people.put(2017, "Paula");
      people.put(2017, "Aimar");
      people.put(2018, "Elaia");

      people.get(2016).whenComplete((v, ex) -> {
         System.out.println(v);
      }).join();

      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
