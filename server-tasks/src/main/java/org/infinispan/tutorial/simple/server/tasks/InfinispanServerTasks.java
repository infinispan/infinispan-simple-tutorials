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
      // Execute and data manipulation caches require different configuration
      // This won't be necessary in the future: https://issues.jboss.org/browse/ISPN-8814
      final RemoteCache<String, String> dataCache = getDataCache();
      final RemoteCache<String, String> execCache = getExecCache();

      // Create task parameters
      Map<String, String> parameters = new HashMap<>();
      parameters.put("name", "developer");

      // Execute hello task
      String greet = execCache.execute("hello-task", parameters);
      System.out.printf("Greeting = %s\n", greet);

      // Store some values and compute the sum
      int range = 10;
      IntStream.range(0, range).boxed().forEach(
         i -> dataCache.put(i + "-key", i + "-value")
      );
      int result = execCache.execute("sum-values-task", Collections.emptyMap());
      System.out.printf("Sum of values = %d\n", result);

      // Stop the cache manager and release all resources
      dataCache.getRemoteCacheManager().stop();
      execCache.getRemoteCacheManager().stop();
   }

   public static RemoteCache<String,String> getDataCache() {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder
         .addServer().host("127.0.0.1").port(11222)
         .marshaller(UTF8StringMarshaller.class);

      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      return cacheManager.getCache();
   }

   public static RemoteCache<String,String> getExecCache() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(11222);

      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      return cacheManager.getCache();
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
