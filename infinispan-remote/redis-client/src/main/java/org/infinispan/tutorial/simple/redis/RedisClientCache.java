package org.infinispan.tutorial.simple.redis;

import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import redis.clients.jedis.Jedis;

public class RedisClientCache {

   static Jedis jedis = null;

   public static void main(String[] args) {
      connect();
      manipulateWithRESP();
      disconnect();
   }

   static void manipulateWithRESP() {
      String key = "Hello";
      jedis.set(key, "world");
      String value = jedis.get(key);
      System.out.println(String.format("Read from Infinispan using a Redis Client (Resp Protocol): %s %s", key, value));
   }

   static void disconnect() {
      TutorialsConnectorHelper.stopInfinispanContainer();
   }

   static void connect() {
      try {
         jedis = createJedis(TutorialsConnectorHelper.SINGLE_PORT);
         jedis.ping();
      } catch (Exception ex) {
         // Unable to connect
         System.out.println("Unable to connect, try to run a container with test containers");
         jedis = null;
      }

      if (jedis == null) {
         TutorialsConnectorHelper.startInfinispanContainer();
         if (TutorialsConnectorHelper.isContainerStarted()) {
            jedis = createJedis(TutorialsConnectorHelper.INFINISPAN_CONTAINER.getFirstMappedPort());
         } else {
            System.out.println("Unable to run Infinispan container");
            System.exit(0);
         }
      }

      try {
         jedis.ping();
      } catch (Exception ex) {
         System.out.println("Unable to ping with RESP");
         System.exit(0);
      }
   }

   private static Jedis createJedis(int port) {
      String redisUri = String.format("redis://%s:%s@%s:%s",
              TutorialsConnectorHelper.USER,
              TutorialsConnectorHelper.PASSWORD,
              TutorialsConnectorHelper.HOST,
              port);
      return new Jedis(redisUri);
   }

}
