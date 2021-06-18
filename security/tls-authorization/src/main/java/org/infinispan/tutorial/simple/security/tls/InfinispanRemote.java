package org.infinispan.tutorial.simple.security.tls;

import java.util.Arrays;
import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

/**
 * Different client configurations that demonstrate SSL/TLS encryption with
 * server and client certificates as well as security authorization.
 *
 * @author <a href="mailto:WolfDieter.Fink@gmail.com">Wolf-Dieter Fink</a>
 *
 */
public class InfinispanRemote {

   private static void testCache(ConfigurationBuilder builder) {
      RemoteCacheManager cacheManager = null;
      try {
         cacheManager = new RemoteCacheManager(builder.build());
         // Obtain the remote cache
         RemoteCache<String, String> cache = cacheManager.getCache("secured");
         testCache(cache);
         System.out.println();
      } catch (Throwable e) {
         e.printStackTrace();
         System.out.println("\n   FAILED \n\n");
      }finally {
         // Stop the cache manager and release all resources
         if(cacheManager != null) cacheManager.stop();
      }
   }

   private static void testCache(RemoteCache<String,String> cache) {
      try {
         cache.put("test", "test");
         System.out.println("    = put succeeds");
      }catch(Exception e) {
         System.out.println("    = put failed  " + e.getMessage());
      }

      try {
         cache.get("test");
         System.out.println("    = get succeeds");
      }catch(Exception e) {
         System.out.println("    = get failed  " + e.getMessage());
      }
   }

   private static void simpleClient1Auth() {
      System.out.println("  - Try to access the server with simple tls server keystore and client1 authentication");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server-truststore.pfx").trustStorePassword("trustSecret".toCharArray())
      .keyStoreFileName("client1-keystore.pfx").keyStorePassword("Client1secret".toCharArray())
      .authentication().saslMechanism("EXTERNAL");

      testCache(builder);
   }

   private static void simpleClient2Auth() {
      System.out.println("  - Try to access the server with simple tls server keystore");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server-truststore.pfx").trustStorePassword("trustSecret".toCharArray())
      .keyStoreFileName("client2-keystore.pfx").keyStorePassword("Client2secret".toCharArray())
      .authentication().saslMechanism("EXTERNAL");

      testCache(builder);
   }

   private static void simpleClientAuth() {
      simpleClient1Auth();
      simpleClient2Auth();
   }

   private static void simpleServerKeystore() {
      System.out.println("  - Try to access the server with simple tls server keystore");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server-truststore.pfx").trustStorePassword("trustSecret".toCharArray());

      testCache(builder);
   }

   private static void signedClient1Keystore() {
      System.out.println("\n\n  - Try to access the server with a signed tls server and client1 keystore");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server_truststore.p12").trustStorePassword("ServerTrustsecret".toCharArray())
      .keyStoreFileName("client1_keystore.p12").keyStorePassword("Client1secret".toCharArray());

      testCache(builder);
   }

   private static void signedClient2Keystore() {
      System.out.println("\n\n  - Try to access the server with a signed tls server and client2 keystore");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server_truststore.p12").trustStorePassword("ServerTrustsecret".toCharArray())
      .keyStoreFileName("client2_keystore.p12").keyStorePassword("Client2secret".toCharArray());

      testCache(builder);
   }

   private static void signedClient1KeystoreAuth() {
      System.out.println("\n\n  - Try to access the server with a signed tls server and client1 keystore with Authorization");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server_truststore.p12").trustStorePassword("ServerTrustsecret".toCharArray())
      .keyStoreFileName("client1_keystore.p12").keyStorePassword("Client1secret".toCharArray());
      builder.security().authentication().saslMechanism("EXTERNAL");

      testCache(builder);
   }

   private static void signedClient2KeystoreAuth() {
      System.out.println("\n\n  - Try to access the server with a signed tls server and client2 keystore with Authorization");
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT).maxRetries(0)
      .security().ssl().trustStoreFileName("server_truststore.p12").trustStorePassword("ServerTrustsecret".toCharArray())
      .keyStoreFileName("client2_keystore.p12").keyStorePassword("Client2secret".toCharArray());
      builder.security().authentication().saslMechanism("EXTERNAL");

      testCache(builder);
   }

   private static boolean shouldRun(String env, String testName) {
      if(env == null || env.isEmpty()) return true;

      List<String> tests = Arrays.asList(env.split(","));
      return tests.contains(testName);
   }

   public static void main(String[] args) {
      String test = System.getenv("TEST");
      System.out.println("  ======================= Start client(s)");
      if(shouldRun(test, "SIMPLE")) simpleServerKeystore();
      if(shouldRun(test, "SIMPLEAUTH")) simpleClientAuth();
      if(shouldRun(test, "CLIENT1")) signedClient1Keystore();
      if(shouldRun(test, "CLIENT2")) signedClient2Keystore();
      if(shouldRun(test, "CLIENT1AUTH")) signedClient1KeystoreAuth();
      if(shouldRun(test, "CLIENT2AUTH")) signedClient2KeystoreAuth();
   }

}
