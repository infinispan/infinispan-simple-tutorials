package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.query.Query;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;
import java.util.Arrays;
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
   public static final String INDEXED_TEAM_CACHE = "indexedTeamCache";
   static RemoteCacheManager client;
   static RemoteCache<PersonKey, Person> peopleCache;
   static RemoteCache<String, Team> teamCache;

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      addDataToPeopleCache();
      queryAllPeople();
      queryWithWhereStatementOnValues();
      queryByKey();
      queryWithProjection();
      deleteByQuery();

      addDataToTeamCache();
      countAllTeam();
      queryWithScoreAndFilterOnNestedValues();
      disconnect(false);
   }

   static List<Person> queryAllPeople() {
      // Query all
      Query<Person> query = peopleCache.query("FROM tutorial.Person");
      List<Person> queryResult = query.execute().list();
      // Print the results
      System.out.println("SIZE " + queryResult.size());
      System.out.println(queryResult);
      return queryResult;
   }

   static Long countAllTeam() {
      // Query all
      Query<Object[]> query = teamCache.query("select count(t) FROM tutorial.Team t");
      List<Object[]> queryResult = query.execute().list();
      // Print the results
      if (queryResult.size() > 0) {
         System.out.println("COUNT " + queryResult.get(0)[0]);
         return (Long) queryResult.get(0)[0];
      }
      System.out.println("No team found");
      return 0L;
   }

   static List<Person> deleteByQuery() {
      Query<Person> query = peopleCache.query("DELETE FROM tutorial.Person p where p.key.pseudo = 'dmalfoy'");
      System.out.println("== DELETE count:" + query.execute().count().value());
      // Query all
      query = peopleCache.query("FROM tutorial.Person");
      List<Person> queryResult = query.execute().list();
      // Print the results
      System.out.println("SIZE " + queryResult.size());
      System.out.println(queryResult);
      return queryResult;
   }

   static List<PersonDTO> queryWithProjection() {
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
      return queryResultProjection;
   }

   static List<Person> queryByKey() {
      // Create a query by key
      System.out.println("== Query by key values");
      Query<Person> query = peopleCache.query("FROM tutorial.Person p where p.key.pseudo = 'dmalfoy'");
      // Execute the query
      List<Person> queryResult = query.execute().list();
      // Print the results queryResult
      System.out.println(queryResult);
      return queryResult;
   }

   static List<Person> queryWithWhereStatementOnValues() {
      // Create a query with lastName parameter
      System.out.println("== Query on values");
      Query<Person> query = peopleCache.query("FROM tutorial.Person p where p.lastName = :lastName");
      // Set the parameter value
      query.setParameter("lastName", "Granger");
      // Execute the query
      List<Person> queryResult = query.execute().list();
      // Print the results
      System.out.println(queryResult);
      return queryResult;
   }

   static void queryWithScoreAndFilterOnNestedValues() {
      System.out.println("== Query and filter on nested values of the team");
      Query<Team> query = teamCache.query("FROM tutorial.Team t join t.players p where p.bornIn = :bornIn");
      // Set the parameter value
      query.setParameter("bornIn", "London");
      List<Team> teamsWithPeopleFromLondon = query.execute().list();
      System.out.println("There are only 2 teams " + teamsWithPeopleFromLondon.size());
      System.out.println(teamsWithPeopleFromLondon);

      // Get the score
      Query<Object[]> queryWithProjectionAndScore = teamCache.query("select t.teamName, score(t) FROM tutorial.Team t where t.points=88");
      List<Object[]> results = queryWithProjectionAndScore.execute().list();
      System.out.println("Team with 88 points: " + results.size());
      System.out.println("Team name: " + results.get(0)[0]);
      System.out.println("Score: " + results.get(0)[1]);

      // With filter in nested values
      Query<Object[]> queryProjection = teamCache.query("select t.teamName, score(t) FROM tutorial.Team t join t.players p where p.lastName = :lastName");
      queryProjection.setParameter("lastName", "Granger");
      results = queryProjection.execute().list();
      System.out.println("Hermione number of teams: " + results.size());
      System.out.println("Hermione team name: " + results.get(0)[0]);
   }


   static void connectToInfinispan() throws Exception {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      // Add the Protobuf serialization context in the client
      builder.addContextInitializer(new TutorialSchemaImpl());

      // Use indexed cache
      URI indexedCacheURI = InfinispanRemoteQuery.class.getClassLoader().getResource("indexedCache.xml").toURI();
      URI teamCacheURI = InfinispanRemoteQuery.class.getClassLoader().getResource("teamCache.xml").toURI();
      builder.remoteCache(INDEXED_PEOPLE_CACHE).configurationURI(indexedCacheURI);
      builder.remoteCache(INDEXED_TEAM_CACHE).configurationURI(teamCacheURI);

      // Connect to the server
      client = TutorialsConnectorHelper.connect(builder);

      // Create and add the Protobuf schema in the server
      addPersonSchema(client);

      // Get the people cache, create it if needed with the default configuration
      peopleCache = client.getCache(INDEXED_PEOPLE_CACHE);
      teamCache = client.getCache(INDEXED_TEAM_CACHE);
   }

   static void addDataToPeopleCache() {
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
   }

   static void addDataToTeamCache() {
      // Create the persons dataset to be stored in the cache
      Map<String, Team> teams = new HashMap<>();

      Team team1 = new Team("Heroic team", 900, Arrays.asList(
              new Person("Hermione", "Granger", 1990, "London"),
              new Person("Neville", "Longbottom", 1990, "Manchester"),
              new Person("Luna", "Lovegood", 1991, "London")
      ));

      Team team2 = new Team("Villains team", 88, Arrays.asList(
              new Person("Draco", "Malfoy", 1988, "London"),
              new Person("Bellatrix", "Lestrange", 1955, "Bristol"),
              new Person("Dolores", "Umbridge", 1950, "Bristol")
      ));

      Team team3 = new Team("I don't care team", 1500, Arrays.asList(
              new Person("Lavender", "Brown", 1990, "Bristol"),
              new Person("Seamus", "Finnigan", 1990, "Manchester")
      ));

      teams.put("1", team1);
      teams.put("2", team2);
      teams.put("3", team3);

      teamCache.putAll(teams);
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         client.administration().removeCache(INDEXED_PEOPLE_CACHE);
      }

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
