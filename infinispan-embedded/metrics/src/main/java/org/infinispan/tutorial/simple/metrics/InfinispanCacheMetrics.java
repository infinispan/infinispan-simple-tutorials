package org.infinispan.tutorial.simple.metrics;

import java.util.Objects;
import java.util.UUID;

import javax.management.ObjectName;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.jmx.MBeanServerLookup;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.metrics.impl.MetricsRegistry;

public class InfinispanCacheMetrics {
    public static final String CACHE_NAME = "my-cache";
    public static final String CONTENT_TYPE = "text/plain; version=0.0.4; charset=utf-8";
    public static final String JMX_DOMAIN = InfinispanCacheMetrics.class.getName();

    public DefaultCacheManager dcm;
    Cache<String, String> cache;

    public static void main(String[] args) throws Throwable {
        System.out.println("Starting process: " + ProcessHandle.current().pid());
        InfinispanCacheMetrics metrics = new InfinispanCacheMetrics();
        metrics.createDefaultCacheManager();
        metrics.createCacheWithMetricsAndPopulate(10);

        // Extract the metrics in the Prometheus format
        String content = metrics.scrapePrometheusMetrics();
        System.out.println(content);

        // Extract with JMX the number of running caches.
        System.out.println("Running cache count:");
        System.out.println(metrics.extractRunningCachesNumber());

        // You can uncomment the line below so the process won't exit.
        // Utilizing the JDK Mission Control it is possible to inspect the beans.
        //Thread.sleep(500_000);

        metrics.stop();
    }

    public void createDefaultCacheManager() {
        GlobalConfigurationBuilder builder = GlobalConfigurationBuilder.defaultClusteredBuilder();

        // Toggle statistics for the cache manager.
        // Equivalent of the following in the XML:
        //   <cache-container statistics="true">
        builder.cacheContainer().statistics(true);

        // Also enable JMX to scrap metrics.
        // Equivalent of the following XML:
        //   <jmx enabled="true" domain="${JMX_DOMAIN}"/>
        builder.jmx().enabled(true).domain(JMX_DOMAIN);

        dcm = new DefaultCacheManager(builder.build());
        dcm.start();
    }

    public void createCacheWithMetricsAndPopulate(int size) {
        // Create a distributed cache with statistics enabled.
        // This is equivalent to the following XML configuration:
        //   <distributed-cache name="my-cache" statistics="true"/>
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.clustering().cacheMode(CacheMode.DIST_SYNC);
        builder.statistics().enabled(true);

        cache = dcm.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
                .getOrCreateCache(CACHE_NAME, builder.build());

        // Insert the entries in the cache.
        for (int i = 0; i < size; i++) {
            cache.put(UUID.randomUUID().toString(), dcm.getNodeAddress());
        }
    }

    public String scrapePrometheusMetrics() {
        MetricsRegistry registry = GlobalComponentRegistry.componentOf(dcm, MetricsRegistry.class);
        Objects.requireNonNull(registry, "Registry is null");

        if (!registry.supportScrape())
            throw new IllegalStateException("Scrape not supported");

        return registry.scrape(CONTENT_TYPE);
    }

    public Object extractRunningCachesNumber() throws Throwable {
        // Retrieve the server to lookup the attributes.
        MBeanServerLookup server = dcm.getCacheManagerConfiguration().jmx().mbeanServerLookup();
        Objects.requireNonNull(server, "MBeanServerLookup is null");

        // Create the name to access the cache manager attributes.
        ObjectName name = new ObjectName(String.format("%s:component=CacheManager,name=\"DefaultCacheManager\",type=CacheManager", JMX_DOMAIN));

        // Retrieve the number of running caches.
        return server.getMBeanServer().getAttribute(name, "RunningCacheCount");
    }

    public void stop() {
        if (dcm != null) {
            dcm.stop();
            dcm = null;
        }
    }
}
