package org.infinispan.tutorial.simple.redis;

import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;
import redis.clients.jedis.JedisPooled;

public class RedisClientCache {

   public static void main(String[] args) {
      String redisUri = String.format("redis://%s:%s@%s:%s",
            TutorialsConnectorHelper.USER,
            TutorialsConnectorHelper.PASSWORD,
            TutorialsConnectorHelper.HOST,
            TutorialsConnectorHelper.SINGLE_PORT);

      JedisPooled jedis = new JedisPooled(redisUri);
      String key = "Hello";
      jedis.set(key, "world");
      String value = jedis.get(key);
      System.out.println(String.format("Read from Infinispan using a Redis Client (Resp Protocol): %s %s", key, value));
   }

}
