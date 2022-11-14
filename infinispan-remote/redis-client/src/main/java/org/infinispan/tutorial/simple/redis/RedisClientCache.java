package org.infinispan.tutorial.simple.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

public class RedisClientCache {

   public static void main(String[] args) {
      String redisUri = String.format("redis://%s:%s@%s:%s",
            TutorialsConnectorHelper.USER,
            TutorialsConnectorHelper.PASSWORD,
            TutorialsConnectorHelper.HOST,
            TutorialsConnectorHelper.SINGLE_PORT);
      RedisClient client = RedisClient.create(redisUri);
      RedisCommands<String, String> redisCommands = client.connect().sync();
      String key = "Hello";
      redisCommands.set(key, "world");
      String value = redisCommands.get(key);
      System.out.println(String.format("Read from Infinispan using a Redis Client (Resp Protocol): %s %s", key, value));
   }

}
