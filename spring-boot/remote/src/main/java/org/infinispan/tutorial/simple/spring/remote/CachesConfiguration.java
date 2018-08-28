package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CachesConfiguration {

   @Bean
   @Qualifier("defaultCache")
   public RemoteCache<String, String> defaultCache(RemoteCacheManager cacheManager) {
      return cacheManager.getCache("default");
   }

}
