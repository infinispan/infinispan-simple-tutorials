package org.infinispan.tutorial.simple.security.tls;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

/**
 * Maps Test env variable name and the SSL configuration to be used on the test
 */
public enum SSLConfig {
   SIMPLE {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("  - Try to access the server with simple tls server keystore");
         builder
               .security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server-truststore.pfx")
               .trustStorePassword("trustSecret".toCharArray());
         return builder;
      }
   },
   SIMPLEAUTH1 {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("  - Try to access the server with simple tls server keystore and client1 authentication");
         builder.security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server-truststore.pfx")
               .trustStorePassword("trustSecret".toCharArray())
               .keyStoreFileName("client1-keystore.pfx").keyStorePassword("Client1secret".toCharArray())
               .authentication().saslMechanism("EXTERNAL");
         return builder;
      }
   },
   SIMPLEAUTH2 {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("  - Try to access the server with simple tls server keystore and client2 authentication");
         builder.security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server-truststore.pfx")
               .trustStorePassword("trustSecret".toCharArray())
               .keyStoreFileName("client2-keystore.pfx").keyStorePassword("Client2secret".toCharArray())
               .authentication().saslMechanism("EXTERNAL");

         return builder;
      }
   },
   CLIENT1 {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("\n\n  - Try to access the server with a signed tls server and client1 keystore");
         // Create a configuration for a locally-running server
         builder
               .security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server_truststore.p12")
               .trustStorePassword("ServerTrustsecret".toCharArray())
               .keyStoreFileName("client1_keystore.p12")
               .keyStorePassword("Client1secret".toCharArray());

         return builder;
      }
   },
   CLIENT2 {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("\n\n  - Try to access the server with a signed tls server and client2 keystore");
         builder.security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server_truststore.p12")
               .trustStorePassword("ServerTrustsecret".toCharArray())
               .keyStoreFileName("client2_keystore.p12")
               .keyStorePassword("Client2secret".toCharArray());
         return builder;
      }
   },
   CLIENT1AUTH {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("\n\n  - Try to access the server with a signed tls server and client1 keystore with Authorization");
         builder.security()
               .ssl().hostnameValidation(false)
               .trustStoreFileName("server_truststore.p12")
               .trustStorePassword("ServerTrustsecret".toCharArray())
               .keyStoreFileName("client1_keystore.p12")
               .keyStorePassword("Client1secret".toCharArray());

         builder.security().authentication().saslMechanism("EXTERNAL");
         return builder;
      }
   },
   CLIENT2AUTH {
      @Override
      public ConfigurationBuilder addConfig(ConfigurationBuilder builder) {
         System.out.println("\n\n  - Try to access the server with a signed tls server and client2 keystore with Authorization");
         builder.security()
               .ssl().hostnameValidation(false).trustStoreFileName("server_truststore.p12")
               .trustStorePassword("ServerTrustsecret".toCharArray())
               .keyStoreFileName("client2_keystore.p12")
               .keyStorePassword("Client2secret".toCharArray());

         builder.security().authentication().saslMechanism("EXTERNAL");
         return builder;
      }
   };

   public abstract ConfigurationBuilder addConfig(ConfigurationBuilder builder);
}
