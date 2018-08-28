package org.infinispan.tutorial.simple.spring.embedded;

import java.lang.invoke.MethodHandles;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * This example shows how to configure Spring Boot and Infinispan
 *
 * <p>It is recommended to use <code>-Djava.net.preferIPv4Stack=true</code> for running multiple instances</p>
 */
@SpringBootApplication
public class SpringBootApp {

    private static final String CACHE_NAME = "test";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * This bean is optional but it shows how to inject {@link org.infinispan.configuration.global.GlobalConfiguration}.
     */
    @Bean
    public InfinispanGlobalConfigurer globalConfiguration() {
        logger.info("Defining Global Configuration");
        return () -> GlobalConfigurationBuilder
              .defaultClusteredBuilder()
              .globalJmxStatistics().allowDuplicateDomains(true)
              .build();
    }

    /**
     * Here we inject {@link Configuration}.
     */
    @Bean
    public InfinispanCacheConfigurer cacheConfigurer() {
        logger.info("Defining {} configuration", CACHE_NAME);
        return manager -> {
            Configuration ispnConfig = new ConfigurationBuilder()
                  .clustering().cacheMode(CacheMode.DIST_SYNC)
                  .build();

            manager.defineConfiguration(CACHE_NAME, ispnConfig);
        };
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringBootApp.class, args);

        EmbeddedCacheManager cacheManager = ctx.getBean(EmbeddedCacheManager.class);
        Cache<Long, String> cache = cacheManager.getCache(CACHE_NAME);
        cache.put(System.currentTimeMillis(), "Infinispan");

        logger.info("Keys from Cache: {}", cache.keySet());
        logger.info("Values from Cache: {}", cache.values());
    }

}
