package org.infinispan.tutorial.simple.scripting;

import java.util.HashMap;
import java.util.Map;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

public class InfinispanScripting {

   public static void main(String[] args) {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT);
      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
      // Retrieve the cache containing the scripts
      RemoteCache<String, String> scriptCache = cacheManager.getCache("___script_cache");
      // Create a simple script which multiplies to numbers
      scriptCache.put("simple.js", "multiplicand * multiplier");
      // Obtain the remote cache
      RemoteCache<String, Integer> cache = cacheManager.getCache();
      // Create the parameters for script execution
      Map<String, Object> params = new HashMap<>();
      params.put("multiplicand", 10);
      params.put("multiplier", 20);
      // Run the script on the server, passing in the parameters
      Object result = cache.execute("simple.js", params);
      // Print the result
      System.out.printf("Result = %s\n", result);
      // Stop the cache manager and release resources
      cacheManager.stop();
   }

}
