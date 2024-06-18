package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.query.Query;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.impl.BaseQueryFactory;
import org.infinispan.server.resp.commands.connection.SELECT;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;
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

   public static final String INDEXED_PEOPLE_CACHE = "indexedPeopleCache";

   public static void main(String[] args) throws Exception {

      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      // Add the Protobuf serialization context in the client
      builder.addContextInitializer(new TutorialSchemaImpl());

      // Use indexed cache
      URI indexedCacheURI = InfinispanRemoteQuery.class.getClassLoader().getResource("indexedCache.xml").toURI();
      builder.remoteCache(INDEXED_PEOPLE_CACHE).configurationURI(indexedCacheURI);

      // Connect to the server
      RemoteCacheManager client = TutorialsConnectorHelper.connect(builder);

      // Create and add the Protobuf schema in the server
      addPersonSchema(client);

      // Get the people cache, create it if needed with the default configuration
      RemoteCache<PersonKey, Person> peopleCache = client.getCache(INDEXED_PEOPLE_CACHE);

      // Create the persons dataset to be stored in the cache
      Map<PersonKey, Person> people = new HashMap<>();
      people.put(new PersonKey("1", "hgranger"),
              new Person("Hermione", "Granger", 1990, "London"));
      people.put(new PersonKey("2", "hpotter"),
              new Person("Harry", "Potter", 1991, "Godric's Hollow"));
      people.put(new PersonKey("3", "rwesley"),
              new Person("Ron", "Wesley", 1990, "London"));
      people.put(new PersonKey("4", "dmalfoy"),
              new Person("Draco", "Malfoy", 1989, "London"));

      // Put all the values in the cache
      peopleCache.putAll(people);
      // Query all
      Query<Person> query = peopleCache.query("FROM tutorial.Person");
      List<Person> queryResult = query.execute().list();
      // Print the results
      System.out.println("SIZE " + queryResult.size());
      System.out.println(queryResult);

      // Create a query with lastName parameter
      System.out.println("== Query on values");
      query = peopleCache.query("FROM tutorial.Person p where p.lastName = :lastName");
      // Set the parameter value
      query.setParameter("lastName", "Granger");
      // Execute the query
      queryResult = query.execute().list();
      // Print the results
      System.out.println(queryResult);

      // Create a query by key
      System.out.println("== Query by key values");
      query = peopleCache.query("FROM tutorial.Person p where p.key.pseudo = 'dmalfoy'");
      // Execute the query
      queryResult = query.execute().list();
      // Print the results queryResult
      System.out.println(queryResult);

      // Create a query with projection
      System.out.println("== Query with key and values projection");
      Query<Object[]> queryProjection = peopleCache.query("SELECT p.key.pseudo, p.firstName, p.lastName FROM tutorial.Person p where p.bornIn = 'London'");
      // Execute the queryProjection
      List<PersonDTO> queryResultProjection = queryProjection.execute().list()
              .stream()
              .map(r -> new PersonDTO(r[0] + "", r[1] + " " + r[2]))
              .toList();
      // Print the results queryResultProjection
      System.out.println(queryResultProjection);

      query = peopleCache.query("DELETE FROM tutorial.Person p where p.key.pseudo = 'dmalfoy'");
      System.out.println("== DELETE count:" + query.execute().count().value());
      // Query all
      query = peopleCache.query("FROM tutorial.Person");
      queryResult = query.execute().list();
      // Print the results
      System.out.println("SIZE " + queryResult.size());
      System.out.println(queryResult);

      // Stop the client and release all resources
      TutorialsConnectorHelper.stop(client);
   }

   record PersonDTO(String pseudo, String fullName){}

   private static void addPersonSchema(RemoteCacheManager cacheManager) {
      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // Define the new schema on the server too
      GeneratedSchema schema = new TutorialSchemaImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
   }
}
