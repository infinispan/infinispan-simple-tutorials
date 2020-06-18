package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.api.continuous.ContinuousQuery;
import org.infinispan.query.api.continuous.ContinuousQueryListener;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.io.IOException;
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

   private static final String CACHE_NAME = "instaposts";

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
               .host("127.0.0.1")
               .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
             .security().authentication()
               //Add user credentials.
               .username("username")
               .password("password")
               .realm("default")
               .saslMechanism("DIGEST-MD5");

      // Connect to the server
      RemoteCacheManager client = new RemoteCacheManager(builder.build());

      // Get the cache, create it if needed with an existing template name
      RemoteCache<String, InstaPost> instaPostsCache = client.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE).getOrCreateCache(CACHE_NAME, DefaultTemplate.DIST_SYNC);

      // Create and add the Protobuf schema for InstaPost class. Note InstaPost is an annotated POJO
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
      List<InstaPost> queryPosts = new ArrayList<>();
      ContinuousQueryListener<String, InstaPost> listener =
            new ContinuousQueryListener<String, InstaPost>() {
               // This method will be executed every time new items that correspond with the query arrive
               @Override
               public void resultJoining(String key, InstaPost post) {
                  System.out.println(String.format("@%s has posted again! Hashtag: #%s", post.user, post.hashtag));
                  queryPosts.add(post);
               }
            };

      // And the listener corresponding the query to the continuous query
      continuousQuery.addContinuousQueryListener(query, listener);

      // Add 1000 random posts
      for (int i = 0; i < 1000; i++) {
         // Add a post
         addRandomPost(instaPostsCache);

         // Await a little to see results
         Thread.sleep(10);
      }

      System.out.println("Total posts " + instaPostsCache.size());
      System.out.println("Total posts by @belen_esteban " + queryPosts.size());

      // Remove the listener. Listeners should be removed when they are no longer needed to avoid memory leaks
      continuousQuery.removeContinuousQueryListener(listener);

      // Remove the cache
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
      // Get the serialization context of the client
      SerializationContext ctx = MarshallerUtil.getSerializationContext(cacheManager);

      // Use ProtoSchemaBuilder to define a Protobuf schema on the client
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

      // Define the new schema on the server too
      metadataCache.putIfAbsent(fileName, protoFile);
   }
}
