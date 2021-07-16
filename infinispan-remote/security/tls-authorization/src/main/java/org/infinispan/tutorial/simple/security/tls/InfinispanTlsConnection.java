package org.infinispan.tutorial.simple.security.tls;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.tutorial.simple.connect.Infinispan;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Different client configurations that demonstrate SSL/TLS encryption with
 * server and client certificates as well as security authorization.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 *
 */
public class InfinispanTlsConnection {

   public static void main(String[] args) {
      System.out.println("  Read the System env TEST if such exist.");
      String testEnv = System.getenv("TEST");

      List<SSLConfig> sslConfigsToTest;
      if(testEnv == null || testEnv.isEmpty()) {
         System.out.println("  TEST env not defined. Test all SSL configurations.");
         sslConfigsToTest = List.of(SSLConfig.values());
      } else {
         System.out.println("  TEST env defined: " + testEnv);
         sslConfigsToTest = Arrays.stream(testEnv.split(","))
               .map(SSLConfig::valueOf)
               .collect(Collectors.toList());
      }
      System.out.println("  Run tests " + sslConfigsToTest);

      System.out.println("  ======================= Start client(s)");
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host(Infinispan.HOST)
            .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
            .maxRetries(0);

      sslConfigsToTest.forEach(sslConfig -> {
         testSslConfig(sslConfig.addConfig(builder));
      });
   }

   private static void testSslConfig(ConfigurationBuilder builder) {
      RemoteCacheManager cacheManager = null;
      try {
         cacheManager = new RemoteCacheManager(builder.build());
         RemoteCache<String, String> cache = cacheManager.getCache("secured");
         System.out.println("    = connection success");
         cache.put("test", "test");
         System.out.println("    = put succeeds");
         cache.get("test");
         System.out.println("    = get succeeds");
      } catch (Throwable e) {
         e.printStackTrace();
         System.out.println("\n   FAILED \n\n");
      } finally {
         // Stop the cache manager and release all resources
         if (cacheManager != null)
            cacheManager.stop();
      }
   }
}
