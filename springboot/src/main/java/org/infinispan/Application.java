package org.infinispan;

import java.lang.invoke.MethodHandles;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ClusteringConfigurationBuilder;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import infinispan.autoconfigure.InfinispanCacheConfigurer;

@SpringBootApplication
@ComponentScan
public class Application {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private static final String CACHE_DEFINED_IN_BEAN = "local-config-from-bean";
   private static final String CACHE_DEFINED_IN_XML = "local-config-from-xml";

   @Configuration
   static class InfinispanConfiguration {

      @Bean
      public InfinispanCacheConfigurer cacheConfigurer() {
         return manager -> {
            ClusteringConfigurationBuilder ispnConfig = new ConfigurationBuilder()
                  .clustering()
                  .cacheMode(CacheMode.LOCAL);

            manager.defineConfiguration(CACHE_DEFINED_IN_BEAN, ispnConfig.build());
         };
      }
   }

   public static void main(String[] args) {
      ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);

      EmbeddedCacheManager cacheManager = applicationContext.getBean(EmbeddedCacheManager.class);

      cacheManager.getCache(CACHE_DEFINED_IN_BEAN).put("test", "forBeanConfiguration");
      cacheManager.getCache(CACHE_DEFINED_IN_XML).put("test", "forXMLConfiguration");

      logger.info("Values in Cache configured by bean: {}", cacheManager.getCache(CACHE_DEFINED_IN_BEAN).values());
      logger.info("Values in Cache configured by XML: {}", cacheManager.getCache(CACHE_DEFINED_IN_XML).values());
   }
}
