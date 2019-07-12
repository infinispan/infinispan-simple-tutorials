package org.infinispan.tutorial.simple.spring.embedded;

import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManagerFactoryBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This example shows how to use Spring's {@link Cacheable} annotation on slow methods.
 */
public class SpringCaching {

    @Configuration
    @EnableCaching
    public static class SpringConfiguration {

        @Bean
        public SpringEmbeddedCacheManagerFactoryBean springCache() {
            return new SpringEmbeddedCacheManagerFactoryBean();
        }

        @Bean
        public CachedObject cachedObject() {
            return new CachedObject();
        }
    }

    public static class CachedObject {

        @Cacheable(value = "default")
        public String verySlowMethod() {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Spring and Infinispan will speed this one up!";
        }
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        CachedObject cachePlayground = applicationContext.getBean(CachedObject.class);

        printWithTime(() -> cachePlayground.verySlowMethod());
        printWithTime(() -> cachePlayground.verySlowMethod());
    }

    private static void printWithTime(Callable<String> functionToCall) throws Exception {
        long startTime = System.currentTimeMillis();
        String result = functionToCall.call();
        System.out.println("Returned: \"" + result + "\" in " + (System.currentTimeMillis() - startTime) / 1000 + " s");
    }
}
