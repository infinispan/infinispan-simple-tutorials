package org.infinispan.tutorial.simple.connect;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

/**
 * Utility class for the simple tutorials in client server mode.
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class Infinispan {

   public static final String USER = "admin";
   public static final String PASSWORD = "password";

   public static final String TUTORIAL_CACHE_NAME = "test";
   public static final String HOST = "127.0.0.1";

   public static final String TUTORIAL_CACHE_CONFIG =
         "<distributed-cache name=\"CACHE_NAME\">\n"
         + "    <encoding media-type=\"application/x-protostream\"/>\n"
         + "</distributed-cache>";

   /**
    * Returns the configuration builder with the connection information
    *
    * @return a Configuration Builder with the connection config
    */
   public static final ConfigurationBuilder connectionConfig() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).security()
            .authentication()
            //Add user credentials.
            .username(USER)
            .password(PASSWORD);

      // Docker 4 Mac Workaround. Don't use BASIC intelligence in production
      builder.clientIntelligence(ClientIntelligence.BASIC);

      // Make sure the remote cache is available.
      // If the cache does not exist, the cache will be created
      builder.remoteCache(TUTORIAL_CACHE_NAME)
            .configuration(TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", TUTORIAL_CACHE_NAME));
      return builder;
   }

   /**
    * Connect to the running Infinispan Server in localhost:11222.
    *
    * This method illustrates how to connect to a running Infinispan Server with a downloaded
    * distribution or a container.
    *
    * @return a connected RemoteCacheManager
    */
   public static final RemoteCacheManager connect() {
      ConfigurationBuilder builder = connectionConfig();

      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      // Clear the cache in case it already exists from a previous running tutorial
      cacheManager.getCache(TUTORIAL_CACHE_NAME).clear();

      // Return the connected cache manager
      return cacheManager;
   }

}
