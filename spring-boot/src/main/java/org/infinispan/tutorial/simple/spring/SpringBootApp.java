package org.infinispan.tutorial.simple.spring;

import java.lang.invoke.MethodHandles;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * This example shows how to configure Spring Boot and Infinispan
 *
 * <p>It is recommended to use <code>-Djava.net.preferIPv4Stack=true</code> for running multiple instances</p>
 */
@SpringBootApplication
public class SpringBootApp {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringBootApp.class, args);

        RemoteCacheManager cacheManager = ctx.getBean(RemoteCacheManager.class);
        RemoteCache<Long, String> cache = cacheManager.getCache();
        cache.put(System.currentTimeMillis(), "Infinispan");

        logger.info("Values from Cache: {}", cache.getBulk());
    }

}
