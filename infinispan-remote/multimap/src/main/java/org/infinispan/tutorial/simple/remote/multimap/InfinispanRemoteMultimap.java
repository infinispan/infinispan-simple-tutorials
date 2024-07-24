package org.infinispan.tutorial.simple.remote.multimap;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.multimap.MultimapCacheManager;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCache;
import org.infinispan.client.hotrod.multimap.RemoteMultimapCacheManagerFactory;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.concurrent.TimeUnit;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

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

   static RemoteCacheManager cacheManager;
   static MultimapCacheManager multimapCacheManager;
   static RemoteMultimapCache<Integer, String> multimap;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      manipulateMultimap();
      disconnect(false);
   }

   static void manipulateMultimap() throws Exception {
      multimap.put(2016, "Rosita");
      multimap.put(2016, "Guillermo");
      multimap.put(2016, "Patricia");
      multimap.put(2016, "Silvia");
      multimap.put(2017, "Matilda");
      multimap.put(2017, "Hector");
      multimap.put(2018, "Richard").get(10, TimeUnit.SECONDS);

      multimap.get(2016).whenComplete((v, ex) -> {
         System.out.println(v);
      }).join();
   }

   public static void connectToInfinispan() {
      // Connect to the server and create a cache
      cacheManager = TutorialsConnectorHelper.connect();

      // Retrieve the MultimapCacheManager from the CacheManager.
      multimapCacheManager = RemoteMultimapCacheManagerFactory.from(cacheManager);

      // Retrieve the multimap cache.
      multimap = multimapCacheManager.get(TUTORIAL_CACHE_NAME);
   }

   public static void disconnect(boolean removeCache) {
      if (removeCache) {
         cacheManager.administration().removeCache(TUTORIAL_CACHE_NAME);
      }
      // Stop the cache manager and release all resources
      TutorialsConnectorHelper.stop(cacheManager);
   }
}
