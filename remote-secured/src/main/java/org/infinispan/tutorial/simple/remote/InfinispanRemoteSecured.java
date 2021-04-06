package org.infinispan.tutorial.simple.remote;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

/**
 * Run the Infinispan Server using the Docker Image
 *
 * docker run -it -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0
 */
public class InfinispanRemoteSecured {

   public static void main(String[] args) {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
      // Workaround for docker 4 mac
      builder.clientIntelligence(ClientIntelligence.BASIC);

      //Configure the security properties
      builder.security().authentication()
              .username("admin")
              .password("pass")
              .saslMechanism("DIGEST-MD5")
              .realm("default")
              .serverName("infinispan");

      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Create test cache, if such does not exist
      cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE).getOrCreateCache("test",
            DefaultTemplate.DIST_SYNC);
      // Obtain the remote cache
      RemoteCache<String, String> cache = cacheManager.getCache("test");
      /// Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
