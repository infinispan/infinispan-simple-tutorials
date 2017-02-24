package org.infinispan;

import infinispan.autoconfigure.InfinispanCacheConfigurer;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	@Bean
	public InfinispanCacheConfigurer cacheConfigurer() {
		return manager -> {
			final Configuration ispnConfig = new ConfigurationBuilder()
					.clustering()
					.cacheMode(CacheMode.LOCAL)
					.build();

			manager.defineConfiguration("local-config-from-bean", ispnConfig);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
