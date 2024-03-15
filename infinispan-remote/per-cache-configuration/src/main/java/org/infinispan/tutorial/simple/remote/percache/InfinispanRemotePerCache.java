package org.infinispan.tutorial.simple.remote.percache;

import org.infinispan.client.hotrod.DefaultTemplate;
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

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      //Add per-cache configuration that uses an org.infinispan cache template.
      builder.remoteCache("my-cache")
            .templateName(DefaultTemplate.DIST_SYNC);
      //Add per-cache configuration with a cache definition in XML format.
      builder.remoteCache("another-cache")
            .configuration("<distributed-cache name=\"another-cache\"><encoding media-type=\"application/x-protostream\"/></distributed-cache>");

      builder.remoteCache("uri-cache").configurationURI(
            InfinispanRemotePerCache.class.getClassLoader().getResource("cacheConfig.xml").toURI());

      // Connect to the server
      try (RemoteCacheManager cacheManager = TutorialsConnectorHelper.connect(builder)) {
         // Obtain a remote cache that does not exist.
         // Rather than return null, create the cache from a template.
         RemoteCache<String, String> cache = cacheManager.getCache("my-cache");
         /// Store a value
         cache.put("hello", "world");
         // Retrieve the value and print it out
         System.out.printf("key = %s\n", cache.get("hello"));

         RemoteCache<String, String> anotherCache = cacheManager.getCache("another-cache");
         /// Store a value
         anotherCache.put("hello-another", "world-another");
         // Retrieve the value and print it out
         System.out.printf("key = %s\n", anotherCache.get("hello-another"));

         RemoteCache<String, String> uriCache = cacheManager.getCache("uri-cache");
         /// Store a value
         uriCache.put("hello-uri", "world-uri");
         // Retrieve the value and print it out
         System.out.printf("key = %s\n", uriCache.get("hello-uri"));
      }
   }
}
