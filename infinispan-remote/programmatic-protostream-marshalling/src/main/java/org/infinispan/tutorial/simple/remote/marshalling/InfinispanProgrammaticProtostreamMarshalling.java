package org.infinispan.tutorial.simple.remote.marshalling;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;
import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.schema.Schema;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 */
public class InfinispanProgrammaticProtostreamMarshalling {

   static Schema schema = MagazineSchemaCreator.magazineSchema();;
   static RemoteCacheManager client;
   static RemoteCache<String, Magazine> magazineRemoteCache;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();

      manipulateCache();

      disconnect(false);
   }

   static void connectToInfinispan() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      ProtoStreamMarshaller marshaller = new ProtoStreamMarshaller();
      SerializationContext serializationContext = marshaller.getSerializationContext();
      FileDescriptorSource fds = FileDescriptorSource.fromString(schema.getName(), schema.toString());
      serializationContext.registerProtoFiles(fds);
      serializationContext.registerMarshaller(new MagazineMarshaller());
      builder.marshaller(marshaller);
      builder.remoteCache(TUTORIAL_CACHE_NAME);

      // Connect to the server
      client = TutorialsConnectorHelper.connect(builder);

      // Create and add the Protobuf schema in the server
      registerMagazineSchemaInTheServer(client);

      // Get the people cache, create it if needed with the default configuration
      magazineRemoteCache = client.getCache(TUTORIAL_CACHE_NAME);
   }

   static void manipulateCache() {
      // Create the persons dataset to be stored in the cache
      Magazine mag1 = new Magazine("MAD", YearMonth.of(1952, 10), Collections.singletonList("Blob named Melvin"));
      Magazine mag2 = new Magazine("TIME", YearMonth.of(1923, 3),
              Arrays.asList("First helicopter", "Change in divorce law", "Adam's Rib movie released",
                      "German Reparation Payments"));
      Magazine map3 = new Magazine("TIME", YearMonth.of(1997, 4),
              Arrays.asList("Yep, I'm gay", "Backlash against HMOS", "False Hope on Breast Cancer?"));

      magazineRemoteCache.put("first-mad", mag1);
      magazineRemoteCache.put("first-time", mag2);
      magazineRemoteCache.put("popular-time", map3);

      System.out.println(magazineRemoteCache.get("popular-time"));
   }

   private static void registerMagazineSchemaInTheServer(RemoteCacheManager cacheManager) {
      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
              cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // Define the new schema on the server too
      metadataCache.put(schema.getName(), schema.toString());
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches && client != null) {
         client.administration().removeCache(TUTORIAL_CACHE_NAME);
      }

      TutorialsConnectorHelper.stop(client);
   }
}
