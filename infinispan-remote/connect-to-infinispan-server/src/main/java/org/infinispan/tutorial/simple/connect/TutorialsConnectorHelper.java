package org.infinispan.tutorial.simple.connect;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.util.OS;
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
      if (OS.getCurrentOs().equals(OS.MAC_OS) || OS.getCurrentOs().equals(OS.WINDOWS)) {
         builder.clientIntelligence(ClientIntelligence.BASIC);
      }
      // ### Docker 4 Mac and Windows Workaround. Don't use BASIC intelligence in production

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

   public static InfinispanContainer INFINISPAN_CONTAINER;

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
            startInfinispanContainer();
            builder.addServer().host(HOST).port(INFINISPAN_CONTAINER.getFirstMappedPort());
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

   public static InfinispanContainer startInfinispanContainer() {
      try {
         INFINISPAN_CONTAINER = new InfinispanContainer();
         INFINISPAN_CONTAINER.withUser(USER);
         INFINISPAN_CONTAINER.withPassword(PASSWORD);
         INFINISPAN_CONTAINER.start();
      } catch (Exception ex) {
         System.out.println("Unable to start Infinispan container");
         return null;
      }
      return INFINISPAN_CONTAINER;
   }

   public static void stopInfinispanContainer() {
      if (INFINISPAN_CONTAINER != null) {
         INFINISPAN_CONTAINER.stop();
      }
   }

   public static void stop(RemoteCacheManager cacheManager) {
      cacheManager.stop();
      stopInfinispanContainer();
   }

}
