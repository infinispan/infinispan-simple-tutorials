package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

/**
 * The Remote Query simple tutorial.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class InfinispanRemoteQuery {

   private static String CACHE_NAME = "people-remote-query";

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clientIntelligence(ClientIntelligence.BASIC);
      builder.addServer()
               .host("127.0.0.1")
               .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
             .security().authentication()
               //Add user credentials.
               .username("admin")
               .password("password")
               .realm("default")
               .saslMechanism("DIGEST-MD5");
      builder.remoteCache(CACHE_NAME)
              .configuration("<distributed-cache name=\"" + CACHE_NAME + "\">" +
                      "<encoding media-type=\"application/x-protostream\"/>" +
                      "</distributed-cache>");

      // Add the Protobuf serialization context in the client
      builder.addContextInitializer(new QuerySchemaBuilderImpl());

      // Connect to the server
      RemoteCacheManager client = new RemoteCacheManager(builder.build());

      // Create and add the Protobuf schema in the server
      addPersonSchema(client);

      // Get the people cache, create it if needed with the default configuration
      RemoteCache<String, Person> peopleCache = client.getCache(CACHE_NAME);

      // Create the persons dataset to be stored in the cache
      Map<String, Person> people = new HashMap<>();
      people.put("1", new Person("Oihana", "Rossignol", 2016, "Paris"));
      people.put("2", new Person("Elaia", "Rossignol", 2018, "Paris"));
      people.put("3", new Person("Yago", "Steiner", 2013, "Saint-Mandé"));
      people.put("4", new Person("Alberto", "Steiner", 2016, "Paris"));

      // Put all the values in the cache
      peopleCache.putAll(people);

      // Get a query factory from the cache
      QueryFactory queryFactory = Search.getQueryFactory(peopleCache);

      // Create a query with lastName parameter
      Query query = queryFactory.create("FROM tutorial.Person p where p.lastName = :lastName");

      // Set the parameter value
      query.setParameter("lastName", "Rossignol");

      // Execute the query
      List<Person> rossignols = query.execute().list();

      // Print the results
      System.out.println(rossignols);

      // Stop the client and release all resources
      client.stop();
   }

   private static void addPersonSchema(RemoteCacheManager cacheManager) {
      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // Define the new schema on the server too
      GeneratedSchema schema = new QuerySchemaBuilderImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
   }
}
