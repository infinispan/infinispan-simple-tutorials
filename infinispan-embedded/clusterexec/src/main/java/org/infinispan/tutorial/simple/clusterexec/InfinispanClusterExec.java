package org.infinispan.tutorial.simple.clusterexec;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.ClusterExecutor;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanClusterExec {

   public static void main(String[] args) {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());
      ClusterExecutor clusterExecutor = cacheManager.executor();
      clusterExecutor.submitConsumer(cm -> new Random().nextInt(), (address, intValue, exception) ->
              System.out.printf("%s\n", intValue));
      // Shuts down the cache manager and all associated resources
      cacheManager.stop();
   }

}
