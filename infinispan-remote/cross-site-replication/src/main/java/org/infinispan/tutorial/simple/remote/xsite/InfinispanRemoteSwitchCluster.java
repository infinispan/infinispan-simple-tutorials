package org.infinispan.tutorial.simple.remote.xsite;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.StringConfiguration;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows how to add backup clusters to the client and how to switch manually from the default
 * to the backup cluster.
 *
 * Execute after running the docker-compose or the minikube to start 2 clusters (LON and NYC)
 * with Cross Site Replication enabled and the ./create-data.sh script in the docker-compose directory
 */
public class InfinispanRemoteSwitchCluster {
   public static final String XSITE_CACHE = "xsiteCache";
   static RemoteCache<String, String> cache;
   static RemoteCacheManager client;
   static StringBuilder log = new StringBuilder();

   public static void main(String[] args) {
      connectToInfinispan();
      manipulateCacheAndSwitchCluster();
      disconnect(false);
   }

   static void manipulateCacheAndSwitchCluster() {
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
   }

   private static void printCluster(String clusterName, RemoteCache<?,?> cache) {
      String logMessage = clusterName + " members: " + cache.getCacheTopologyInfo().getSegmentsPerServer().keySet().stream().map(Object::toString).collect(
              Collectors.joining(","));
      log.append(logMessage);
      System.out.println(logMessage);
   }

   public static void connectToInfinispan() {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      builder.addCluster("NYC").addClusterNodes("localhost:31223");
      client = TutorialsConnectorHelper.connect(builder);
      cache = client.administration()
              // this cache should exist if you start with docker-compose and run the create-data.sh script
              .getOrCreateCache(XSITE_CACHE, new StringConfiguration("<distributed-cache/>"));

   }

   public static void disconnect(boolean removeCache) {
      if (removeCache) {
         client.administration().removeCache(XSITE_CACHE);
      }

      // Connecting to NYC http://localhost:31222/console/cache/xsiteCache
      // hello (copied from LON) and hello-nyc both exist in NYC
      // hello exists in LON, but hello-nyc is absent because replication is active-passive from LON to NYC
      TutorialsConnectorHelper.stop(client);
   }
}
