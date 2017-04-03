package org.infinispan.tutorial.simple.spark;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.spark.config.ConnectorConfiguration;
import org.infinispan.spark.rdd.InfinispanJavaRDD;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class SimpleSparkJob {

   public static void main(String[] args) throws UnknownHostException {
      // Obtain the Infinispan address
      String infinispanAddress = args[0];

      // Adjust log levels
      Logger.getLogger("org").setLevel(Level.WARN);

      // Create the remote cache manager
      Configuration build = new ConfigurationBuilder().addServer().host(infinispanAddress).build();
      RemoteCacheManager remoteCacheManager = new RemoteCacheManager(build);

      // Obtain the remote cache
      RemoteCache<Integer, Temperature> cache = remoteCacheManager.getCache();

      // Add some data
      cache.put(1, new Temperature(21, "London"));
      cache.put(2, new Temperature(34, "Rome"));
      cache.put(3, new Temperature(33, "Barcelona"));
      cache.put(4, new Temperature(8, "Oslo"));

      // Create java spark context
      SparkConf conf = new SparkConf().setAppName("infinispan-spark-simple-job");
      JavaSparkContext jsc = new JavaSparkContext(conf);

      // Create InfinispanRDD
      ConnectorConfiguration config = new ConnectorConfiguration().setServerList(infinispanAddress);

      JavaPairRDD<Integer, Temperature> infinispanRDD = InfinispanJavaRDD.createInfinispanRDD(jsc, config);

      // Convert RDD to RDD of doubles
      JavaDoubleRDD javaDoubleRDD = infinispanRDD.values().mapToDouble(Temperature::getValue);

      // Calculate average temperature
      Double meanTemp = javaDoubleRDD.mean();
      System.out.printf("\nAVERAGE TEMPERATURE: %f C\n", meanTemp);

      // Calculate standard deviation
      Double stdDev = javaDoubleRDD.sampleStdev();
      System.out.printf("STD DEVIATION: %f C\n ", stdDev);

      // Calculate histogram of temperatures
      System.out.println("TEMPERATURE HISTOGRAM:");
      double[] buckets = {0d, 10d, 20d, 30d, 40d};
      long[] histogram = javaDoubleRDD.histogram(buckets);

      for (int i = 0; i < buckets.length - 1; i++) {
         System.out.printf("Between %f C and %f C: %d cities\n", buckets[i], buckets[i + 1], histogram[i]);
      }
   }

   @SuppressWarnings("unused")
   static class Temperature implements Serializable {

      private double value;
      private final String location;

      public Temperature(double value, String location) {
         this.value = value;
         this.location = location;
      }

      public double getValue() {
         return value;
      }

   }
}
