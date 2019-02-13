package org.infinispan.tutorial.simple.remote.query;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.api.continuous.ContinuousQueryListener;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

/**
 * The Remote Continuous Query simple tutorial.
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

   private static final String CACHE_NAME = "instaposts";

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1")
            .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
            .marshaller(ProtoStreamMarshaller.class); // You need to specify the marshaller

      // Connect to the server
      RemoteCacheManager client = new RemoteCacheManager(builder.build());

      // Get the cache, create it if needed with the default configuration
      RemoteCache<String, InstaPost> instaPostsCache = client.administration().getOrCreateCache(CACHE_NAME, "default");

      // Create and add the marshalling configuration for Person pojo class. Note Person is an annotated POJO
      addInstapostsSchema(client);

      // Get a query factory from the cache
      QueryFactory queryFactory = Search.getQueryFactory(instaPostsCache);

      // Create a query with lastName parameter
      Query query = queryFactory.create("FROM tutorial.InstaPost p where p.user = :userName");

      // Set the parameter value
      query.setParameter("userName", "belen_esteban");

      // Create the continuous query
      ContinuousQuery<String, InstaPost> continuousQuery = Search.getContinuousQuery(instaPostsCache);

      // Create the continuous query listener.
      ContinuousQueryListener<String, InstaPost> listener =
            new ContinuousQueryListener<String, InstaPost>() {
               // This method will be executed every time new items that correspond with the query arrive
               @Override
               public void resultJoining(String id, InstaPost post) {
                  System.out.println(String.format("@%s has posted again! Hashtag: #%s", post.user, post.hashtag));
               }
            };
      // Clear all the listeners
      continuousQuery.removeAllListeners();

      // And the listener corresponding the query to the continuous query
      continuousQuery.addContinuousQueryListener(query, listener);

      // Add 1000 random posts
      for (int i = 0; i < 1000; i++) {
         // Add a post
         addRandomPost(instaPostsCache);

         // Await a little to see results
         Thread.sleep(10);
      }

      // Clear the cache
      client.administration().removeCache(CACHE_NAME);

      // Stop the client and release all resources
      client.stop();
   }

   private static void addRandomPost(RemoteCache<String, InstaPost> cache) {
      String id = UUID.randomUUID().toString();
      Random random = new Random();
      // Create the random post
      InstaPost post = new InstaPost(id, USERS.get(random.nextInt(USERS.size())), HASHTAGS.get(random.nextInt(HASHTAGS.size())));
      // Put a post in the cache
      cache.put(id, post);
   }

   private static void addInstapostsSchema(RemoteCacheManager cacheManager) throws IOException {
      // Get the serialization context
      SerializationContext ctx = ProtoStreamMarshaller.getSerializationContext(cacheManager);

      // Use ProtoSchemaBuilder to define
      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
      String fileName = "instapost.proto";
      String protoFile = protoSchemaBuilder
            .fileName(fileName)
            .addClass(InstaPost.class)
            .packageName("tutorial")
            .build(ctx);

      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // Store the configuration in the metadata cache
      metadataCache.putIfAbsent(fileName, protoFile);
   }
}
