package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.query.ContinuousQuery;
import org.infinispan.commons.api.query.ContinuousQueryListener;
import org.infinispan.commons.api.query.Query;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

/**
 * The Remote Continuous Query simple tutorial.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class InfinispanRemoteContinuousQuery {

   // USERS will be used to create random posts
   private static final List<String> USERS = Arrays.asList(
         "gustavoalle",
         "remerson",
         "anistor",
         "karesti",
         "ttarrant",
         "belen_esteban",
         "dberindei",
         "galderz",
         "wburns",
         "pruivo",
         "oliveira",
         "vrigamonti");

   // HASHTAGS will be used to create random posts
   private static final List<String> HASHTAGS = Arrays.asList(
         "love",
         "instagood",
         "photooftheday",
         "fashion",
         "beautiful",
         "happy",
         "cute",
         "tbt",
         "like4like",
         "followme",
         "infinispan");

   static RemoteCacheManager client;
   static InstaSchemaImpl schema;
   static ContinuousQuery<String, InstaPost> continuousQuery;
   static ContinuousQueryListener<String, InstaPost> listener;
   static List<InstaPost> queryPosts = new ArrayList<>();
   static Random random = new Random();

   public static void main(String[] args) throws Exception {
      connectToInfinispan();
      createPostsAndQuery(1000, true);
      cleanup();
      disconnect();
   }

   private static void cleanup() {
      // Remove the listener. Listeners should be removed when they are no longer needed to avoid memory leaks
      continuousQuery.removeContinuousQueryListener(listener);

      // Remove the cache
      client.administration().removeCache(TutorialsConnectorHelper.TUTORIAL_CACHE_NAME);
   }

   public static void createPostsAndQuery(int size, boolean random) throws Exception {
      RemoteCache<String, InstaPost> cache = client.getCache(TutorialsConnectorHelper.TUTORIAL_CACHE_NAME);

      // Create a query with lastName parameter
      Query<InstaPost> query = cache.query("FROM tutorial.InstaPost p where p.user = :userName");

      // Set the parameter value
      query.setParameter("userName", "belen_esteban");

      // Create the continuous query
      ContinuousQuery<String, InstaPost> continuousQuery = cache.continuousQuery();

      // Create the continuous query listener.
      listener =
              new ContinuousQueryListener<>() {
                 // This method will be executed every time new items that correspond with the query arrive
                 @Override
                 public void resultJoining(String key, InstaPost post) {
                    System.out.println(String.format("@%s has posted again! Hashtag: #%s", post.user(), post.hashtag()));
                    queryPosts.add(post);
                 }
              };

      // And the listener corresponding the query to the continuous query
      continuousQuery.addContinuousQueryListener(query, listener);

      // Add 1000 random posts
      for (int i = 0; i < size; i++) {
         // Add a post
         addPost(cache, random);

         // Await a little to see results
         Thread.sleep(10);
      }

      System.out.println("Total posts " + cache.size());
      System.out.println("Total posts by @belen_esteban " + queryPosts.size());
   }

   private static void addPost(RemoteCache<String, InstaPost> cache, boolean isRandom) {
      String id = UUID.randomUUID().toString();
      InstaPost post;
      if (isRandom) {
         // Create the random post
         post = new InstaPost(id, USERS.get(random.nextInt(USERS.size())), HASHTAGS.get(random.nextInt(HASHTAGS.size())));
      } else {
         post = new InstaPost(id, "belen_esteban", "infinispan");
      }
      // Put a post in the cache
      cache.put(id, post);
   }

   public static void connectToInfinispan() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      schema = new InstaSchemaImpl();
      builder.addContextInitializer(schema);
      client = TutorialsConnectorHelper.connect(builder);
      // Create and add the Protobuf schema for InstaPost class. Note InstaPost is an annotated POJO
      register(schema, client);
   }

   public static void disconnect() {
      // Stop the client and release all resources
      TutorialsConnectorHelper.stop(client);
   }

   private static void register(InstaSchemaImpl schema, RemoteCacheManager cacheManager) {
      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // register the new schema on the server too
      metadataCache.putIfAbsent(schema.getProtoFileName(), schema.getProtoFile());
   }
}
