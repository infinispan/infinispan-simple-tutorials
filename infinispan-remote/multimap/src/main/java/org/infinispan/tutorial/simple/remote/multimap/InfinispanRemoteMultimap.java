package org.infinispan.tutorial.simple.remote.multimap;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.multimap.MultimapCacheManager;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCache;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCacheManagerFactory;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * The Remote Multimap simple tutorial.
 * <p>
 * Remote multimap is available as of Infinispan version 9.2.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class InfinispanRemoteMultimap {

   public static void main(String[] args) throws Exception {
      // Connect to the server and create a cache
      RemoteCacheManager cacheManager = TutorialsConnectorHelper.connect();

      // Retrieve the MultimapCacheManager from the CacheManager.
      MultimapCacheManager multimapCacheManager = RemoteMultimapCacheManagerFactory.from(cacheManager);

      // Retrieve the multimap cache.
      RemoteMultimapCache<Integer, String> people = multimapCacheManager.get(TutorialsConnectorHelper.TUTORIAL_CACHE_NAME);
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
      TutorialsConnectorHelper.stop(cacheManager);
   }

}
