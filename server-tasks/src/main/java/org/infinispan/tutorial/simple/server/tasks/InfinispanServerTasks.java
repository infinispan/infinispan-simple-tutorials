package org.infinispan.tutorial.simple.server.tasks;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.dataconversion.UTF8Encoder;
import org.infinispan.commons.marshall.UTF8StringMarshaller;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InfinispanServerTasks {

   public static void main(String[] args) {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder
         .addServer().host("127.0.0.1").port(11222)
         .marshaller(UTF8StringMarshaller.class); // Data is UTF-8 String encoded

      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      // Obtain the remote cache
      RemoteCache<String, String> cache = cacheManager.getCache();

      // Create task parameters
      Map<String, String> parameters = new HashMap<>();
      parameters.put("name", "developer");

      // Execute hello task
      String greet = cache.execute("hello-task", parameters);
      System.out.printf("Greeting = %s\n", greet);

      // Store some values and compute the sum
      int range = 10;
      IntStream.range(0, range).boxed().forEach(
         i -> cache.put(i + "-key", i + "-value")
      );
      int result = cache.execute("sum-values-task", Collections.emptyMap());
      System.out.printf("Sum of values = %d\n", result);

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

   public static class SumValuesTask implements ServerTask<Integer> {

      private TaskContext ctx;

      @Override
      public void setTaskContext(TaskContext ctx) {
         this.ctx = ctx;
      }

      @Override
      public Integer call() throws Exception {
         Cache<String, String> cache = getCache();

         return cache.keySet()
            .stream()
               .map(e -> Integer.valueOf(e.substring(0, e.indexOf("-"))))
               .collect(() -> Collectors.summingInt(Integer::intValue));
      }

      @Override
      public String getName() {
         return "sum-values-task";
      }

      @SuppressWarnings("unchecked")
      private Cache<String, String> getCache() {
         return (Cache<String, String>) ctx.getCache().get()
            .getAdvancedCache().withEncoding(UTF8Encoder.class);
      }

   }

}
