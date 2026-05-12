package org.infinispan.tutorial.simple.clusterexec;

import java.util.Random;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.ClusterExecutor;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.function.SerializableFunction;
import org.infinispan.util.function.TriConsumer;

public class InfinispanClusterExec {

   DefaultCacheManager cacheManager;

   public static void main(String[] args) {
      InfinispanClusterExec infinispanClusterExec = new InfinispanClusterExec();
      infinispanClusterExec.createCacheManager();
      infinispanClusterExec.submitTask(cm -> new Random().nextInt(),
              (address, intValue, exception) -> System.out.printf("%s\n", intValue));
      infinispanClusterExec.stopDefaultCacheManager();
   }

   // tag::submit-task[]
   public void submitTask(SerializableFunction<EmbeddedCacheManager, Object> task, TriConsumer<Address, Object, Throwable> triConsumer) {
      ClusterExecutor clusterExecutor = cacheManager.executor();
      clusterExecutor.submitConsumer(task, triConsumer);
   }
   // end::submit-task[]

   public void createCacheManager() {
      // tag::cache-manager[]
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Initialize the cache manager
      this.cacheManager = new DefaultCacheManager(global.build());
      // end::cache-manager[]
   }

   public void stopDefaultCacheManager() {
      if (cacheManager != null) {
         // Shuts down the cache manager and all associated resources
         cacheManager.stop();
         cacheManager = null;
      }
   }
}
