package org.infinispan.tutorial.simple.map;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

public class InfinispanMap {

   public static void main(String[] args) {
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      Cache<String, String> cache = cacheManager.getCache();
      cache.put("key", "value");
      System.out.printf("key = %s\n", cache.get("key"));
      cacheManager.stop();
   }

}
