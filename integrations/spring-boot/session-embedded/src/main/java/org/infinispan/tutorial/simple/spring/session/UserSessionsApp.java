package org.infinispan.tutorial.simple.spring.session;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@EnableInfinispanEmbeddedHttpSession
@SpringBootApplication
public class UserSessionsApp {

    @Bean
    public InfinispanGlobalConfigurer globalCustomizer() {
        return () -> GlobalConfigurationBuilder
              .defaultClusteredBuilder()
              .metrics().gauges(false).histograms(false)
              .globalState().disable()
              .build();
    }

    @Bean
    public InfinispanCacheConfigurer cacheConfigurer() {
        return manager -> {
            final Configuration ispnConfig = new ConfigurationBuilder()
                    .clustering()
                    .cacheMode(CacheMode.DIST_SYNC)
                    .statistics().disable()
                    .build();


            manager.defineConfiguration("sessions", ispnConfig);
        };
    }

    public static void main(String... args) {
        new SpringApplicationBuilder().sources(UserSessionsApp.class).run(args);
    }
}
