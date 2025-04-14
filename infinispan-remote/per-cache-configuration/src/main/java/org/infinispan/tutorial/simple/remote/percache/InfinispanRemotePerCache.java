package org.infinispan.tutorial.simple.remote.percache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 *
 * You can define per-cache configuration using org.infinispan templates or
 * cache definitions in XML format. Then, if you invoke the `getCache()` method
 * for a remote cache that does not exist, Infinispan creates the cache with
 * the configuration instead of returning null.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author <a href="mailto:dnaro@redhat.com">Don Naro</a>
 */

public class InfinispanRemotePerCache {

   public static final String MY_CACHE = "my-cache";
   public static final String URI_CACHE = "uri-cache";
   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static RemoteCache<String, String> uriCache;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      manipulateCaches();

      disconnect(false);
   }

   static void manipulateCaches() {
      // Obtain a remote cache that does not exist.
      // Rather than return null, create the cache from a template.
      cache = cacheManager.getCache(MY_CACHE);
      /// Store a value
      cache.put("hello", "world");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("hello"));

      uriCache = cacheManager.getCache(URI_CACHE);
      /// Store a value
      uriCache.put("hello-uri", "world-uri");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", uriCache.get("hello-uri"));
   }

   public static void connectToInfinispan() throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      //Add per-cache configuration with a cache definition in XML format.
      builder.remoteCache(MY_CACHE)
              .configuration("<distributed-cache name=\"another-cache\"><encoding media-type=\"application/x-protostream\"/></distributed-cache>");

      builder.remoteCache(URI_CACHE).configurationURI(
              InfinispanRemotePerCache.class.getClassLoader().getResource("cacheConfig.xml").toURI());

      cacheManager = TutorialsConnectorHelper.connect(builder);
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         cacheManager.administration().removeCache(MY_CACHE);
         cacheManager.administration().removeCache(URI_CACHE);
      }

      TutorialsConnectorHelper.stop(cacheManager);
   }
}
