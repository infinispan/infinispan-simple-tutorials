package org.infinispan.tutorial.simple.distexec;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanDistExec {

   public static void main(String[] args) {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Make the default cache a distributed one
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.clustering().cacheMode(CacheMode.DIST_SYNC);
      // Initialize the cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager(global.build(), builder.build());
      // Obtain the default cache
      Cache<String, String> cache = cacheManager.getCache();
      // Create a distributed executor service using the distributed cache to determine the nodes on which to run
      DefaultExecutorService executorService = new DefaultExecutorService(cache);
      // Submit a job to all nodes
      List<Future<Integer>> results = executorService.submitEverywhere((Callable & Serializable) () -> new Random().nextInt());
      // Print out the results
      results.forEach(s -> {
         try {
            System.out.printf("%s\n", s.get());
         } catch (Exception e) {
         }
      });
      // Shuts down the cache manager and all associated resources
      cacheManager.stop();
   }

}
