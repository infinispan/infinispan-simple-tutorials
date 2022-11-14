package org.infinispan.tutorial.simple.server.tasks;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.rest.RestClient;
import org.infinispan.client.rest.RestEntity;
import org.infinispan.client.rest.RestResponse;
import org.infinispan.client.rest.RestURI;
import org.infinispan.client.rest.configuration.RestClientConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_JAVASCRIPT;
import static org.infinispan.commons.util.Util.getResourceAsString;

public class InfinispanServerTasks {

   public static void main(String[] args) throws Exception {
      RemoteCacheManager remoteCacheManager = TutorialsConnectorHelper.connect();

      // Upload the task using the REST API
      uploadTask();

      // Get a cache to execute the task
      RemoteCache<String, String> execCache = remoteCacheManager.getCache(TutorialsConnectorHelper.TUTORIAL_CACHE_NAME);

      // Create task parameters
      Map<String, String> parameters = new HashMap<>();
      parameters.put("greetee", "developer");

      // Execute hello task
      String greet = execCache.execute("hello", parameters);
      System.out.printf("Greeting = %s\n", greet);

      // Stop the cache manager and release all resources
      remoteCacheManager.stop();
   }

   private static void uploadTask() throws Exception {
      // Grab the script content from the resources folder
      String script = getResourceAsString("hello.js", InfinispanServerTasks.class.getClassLoader());
      // Connect to the locally running Infinispan Server through the REST API
      RestURI uri = RestURI
            .create(String.format("http://localhost:%d", ConfigurationProperties.DEFAULT_HOTROD_PORT));
      RestClientConfigurationBuilder builder = uri.toConfigurationBuilder();
      builder.security().authentication().username(TutorialsConnectorHelper.USER).password(TutorialsConnectorHelper.PASSWORD);
      RestClient client = RestClient.forConfiguration(builder.build());
      RestEntity scriptEntity = RestEntity.create(APPLICATION_JAVASCRIPT, script);
      CompletionStage<RestResponse> uploadScript = client.tasks()
            .uploadScript("hello", scriptEntity);
      uploadScript.toCompletableFuture().get(5, TimeUnit.SECONDS);
   }
}
