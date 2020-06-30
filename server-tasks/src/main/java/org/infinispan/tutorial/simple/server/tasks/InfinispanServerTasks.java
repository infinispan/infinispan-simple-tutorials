package org.infinispan.tutorial.simple.server.tasks;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_JAVASCRIPT_TYPE;
import static org.infinispan.commons.util.Util.getResourceAsString;

public class InfinispanServerTasks {

   public static void main(String[] args) throws Exception {
      // Upload the task using the REST API
      uploadTask();

      // Get a cache to execute the task
      final RemoteCache<String, String> execCache = getExecCache();

      // Create task parameters
      Map<String, String> parameters = new HashMap<>();
      parameters.put("greetee", "developer");

      // Execute hello task
      String greet = execCache.execute("hello-task", parameters);
      System.out.printf("Greeting = %s\n", greet);

      // Stop the cache manager and release all resources
      execCache.getRemoteCacheManager().stop();
   }

   private static void uploadTask() throws IOException {
      String taskPostUrl = String.format("http://localhost:%d/rest/v2/tasks/hello-task", ConfigurationProperties.DEFAULT_HOTROD_PORT);
      String script = getResourceAsString("hello.js", InfinispanServerTasks.class.getClassLoader());
      HttpClient client = new HttpClient();
      client.getParams().setAuthenticationPreemptive(true);
      Credentials credentials = new UsernamePasswordCredentials("username", "password");
      client.getState().setCredentials(AuthScope.ANY, credentials);

      PostMethod postMethod = new PostMethod(taskPostUrl);
      postMethod.setRequestHeader("Content-type", APPLICATION_JAVASCRIPT_TYPE);

      postMethod.setRequestEntity(new StringRequestEntity(script, null, null));
      client.executeMethod(postMethod);
   }

   private static RemoteCache<String,String> getExecCache() {
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

      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      return cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("data", DefaultTemplate.DIST_SYNC);
   }
}
