package org.infinispan.tutorial.simple.server.tasks;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;

import java.util.HashMap;
import java.util.Map;

public class InfinispanServerTasks {

   public static void main(String[] args) {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(11222);
      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Obtain the remote cache
      RemoteCache<String, String> cache = cacheManager.getCache();
      // Create task parameters
      Map<String, String> parameters = new HashMap<>();
      parameters.put("name", "developer");
      // Execute task
      String greet = cache.execute("hello-task", parameters);
      System.out.printf("Greeting = %s\n", greet);
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

   public static class HelloTask implements ServerTask<String> {

      private TaskContext ctx;

      @Override
      public void setTaskContext(TaskContext ctx) {
         this.ctx = ctx;
      }

      @Override
      public String call() throws Exception {
         String name = (String) ctx.getParameters().get().get("name");
         return "Hello " + name;
      }

      @Override
      public String getName() {
         return "hello-task";
      }

   }

}
