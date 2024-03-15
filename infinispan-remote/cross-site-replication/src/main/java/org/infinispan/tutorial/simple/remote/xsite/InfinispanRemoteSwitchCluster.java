package org.infinispan.tutorial.simple.remote.xsite;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.stream.Collectors;

/**
 * Shows how to add backup clusters to the client and how to switch manually from the default
 * to the backup cluster.
 *
 * Execute after running the docker-compose or the minikube to start 2 clusters (LON and NYC)
 * with Cross Site Replication enabled and the ./create-data.sh script in the docker-compose directory
 */
public class InfinispanRemoteSwitchCluster {

   public static void main(String[] args) {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      builder.addCluster("NYC").addClusterNodes("localhost:31223");

      try (RemoteCacheManager client = TutorialsConnectorHelper.connect(builder)) {
         RemoteCache<String, String> cache = client.getCache("xsiteCache");
         cache.put("hello", "world");
         printCluster("LON", cache);
         System.out.println("hello " + cache.get("hello") + " from LON");
         client.switchToCluster("NYC");
         printCluster("NYC", cache);
         System.out.println("hello " + cache.get("hello") + " from NYC");
         cache.put("hello-nyc", "world");
         System.out.println("hello-nyc " + cache.get("hello-nyc") + " from NYC");
         client.switchToDefaultCluster();
         printCluster("LON", cache);
         // Connecting to NYC http://localhost:31222/console/cache/xsiteCache
         // hello (copied from LON) and hello-nyc both exist in NYC
         // hello exists in LON, but hello-nyc is absent because replication is active-passive from LON to NYC
         TutorialsConnectorHelper.stop(client);
      }
   }

   private static void printCluster(String clusterName, RemoteCache<?,?> cache) {
      System.out.println(clusterName + " members: " + cache.getCacheTopologyInfo().getSegmentsPerServer().keySet().stream().map(Object::toString).collect(
            Collectors.joining(",")));
   }
}
