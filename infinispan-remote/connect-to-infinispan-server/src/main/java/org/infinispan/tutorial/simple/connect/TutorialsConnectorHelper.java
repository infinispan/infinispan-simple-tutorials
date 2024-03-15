package org.infinispan.tutorial.simple.connect;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.server.test.core.InfinispanContainer;

/**
 * Utility class for the simple tutorials in client server mode.
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class TutorialsConnectorHelper {

   public static final String USER = "admin";
   public static final String PASSWORD = "password";
   public static final String HOST = "127.0.0.1";
   public static final int SINGLE_PORT = ConfigurationProperties.DEFAULT_HOTROD_PORT;

   public static final String TUTORIAL_CACHE_NAME = "test";
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
      builder.security()
            .authentication()
            //Add user credentials.
            .username(USER)
            .password(PASSWORD);

      // ### Docker 4 Mac Workaround. Don't use BASIC intelligence in production
      /* ### ALERT!! Don't add this line in production by default */ builder.clientIntelligence(ClientIntelligence.BASIC);
      // ### Docker 4 Mac Workaround. Don't use BASIC intelligence in production

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
      // Return the connected cache manager
      return connect(connectionConfig());
   }

   static InfinispanContainer infinispanContainer;

   public static final RemoteCacheManager connect(ConfigurationBuilder builder) {

      RemoteCacheManager cacheManager = null;
      try {
         builder.addServer().host(HOST).port(SINGLE_PORT);
         cacheManager = new RemoteCacheManager(builder.build());
         // Clear the cache in case it already exists from a previous running tutorial
         cacheManager.getCache(TUTORIAL_CACHE_NAME).clear();
      } catch (Exception ex) {
         System.out.println("Unable to connect to a running server in localhost:11222. Try test containers");
         if (cacheManager != null) {
            cacheManager.stop();
         }
         cacheManager = null;
      }

      if (cacheManager == null) {
         try {
            infinispanContainer = new InfinispanContainer();
            infinispanContainer.withUser(USER);
            infinispanContainer.withPassword(PASSWORD);
            infinispanContainer.start();
            builder.addServer().host(HOST).port(infinispanContainer.getFirstMappedPort());
            cacheManager = new RemoteCacheManager(builder.build());
            // Clear the cache in case it already exists from a previous running tutorial
            cacheManager.getCache(TUTORIAL_CACHE_NAME).clear();
         } catch (Exception ex) {
            System.out.println("Infinispan Server start with Testcontainers failed. Exit");
            System.exit(0);
         }
      }
      // Return the connected cache manager
      return cacheManager;
   }

   public static void stop(RemoteCacheManager cacheManager) {
      cacheManager.stop();
      if (infinispanContainer != null) {
         infinispanContainer.stop();
      }
   }

}
