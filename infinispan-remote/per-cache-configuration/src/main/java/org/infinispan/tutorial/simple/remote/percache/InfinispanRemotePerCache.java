package org.infinispan.tutorial.simple.remote.percache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.StringConfiguration;
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
   public static final String ANOTHER_CACHE = "another-cache";
   public static final String URI_CACHE = "uri-cache";
   public static final String MY_CUSTOM_TEMPLATE = "my-custom-template";
   static RemoteCacheManager cacheManager;
   static RemoteCache<String, String> cache;
   static RemoteCache<String, String> anotherCache;
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

      anotherCache = cacheManager.getCache(ANOTHER_CACHE);
      /// Store a value
      anotherCache.put("hello-another", "world-another");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", anotherCache.get("hello-another"));

      uriCache = cacheManager.getCache(URI_CACHE);
      /// Store a value
      uriCache.put("hello-uri", "world-uri");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", uriCache.get("hello-uri"));
   }

   public static void connectToInfinispan() throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      //Add per-cache configuration that uses an org.infinispan cache template.
      builder.remoteCache(MY_CACHE)
               // we can declare a template, even if the template does not exist yet.
               // however, the template has to be present on first access to create the cache.
              .templateName(MY_CUSTOM_TEMPLATE);
      //Add per-cache configuration with a cache definition in XML format.
      builder.remoteCache(ANOTHER_CACHE)
              .configuration("<distributed-cache name=\"another-cache\"><encoding media-type=\"application/x-protostream\"/></distributed-cache>");

      builder.remoteCache(URI_CACHE).configurationURI(
              InfinispanRemotePerCache.class.getClassLoader().getResource("cacheConfig.xml").toURI());

      cacheManager = TutorialsConnectorHelper.connect(builder);
      // create the template that is used to create MY-CACHE on first access
      cacheManager.administration().removeTemplate(MY_CUSTOM_TEMPLATE);
      cacheManager.administration().createTemplate(MY_CUSTOM_TEMPLATE, new StringConfiguration("<distributed-cache><encoding media-type=\"application/x-protostream\"/></distributed-cache>"));
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         cacheManager.administration().removeCache(MY_CACHE);
         cacheManager.administration().removeCache(ANOTHER_CACHE);
         cacheManager.administration().removeCache(URI_CACHE);
      }

      TutorialsConnectorHelper.stop(cacheManager);
   }
}
