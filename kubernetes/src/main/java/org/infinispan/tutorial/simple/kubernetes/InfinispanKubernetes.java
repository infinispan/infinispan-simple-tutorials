package org.infinispan.tutorial.simple.kubernetes;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

public class InfinispanKubernetes {

    public static void main(String[] args) throws UnknownHostException {
        //Configure Infinispan to use default transport and Kubernetes configuration
        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder().transport()
                .defaultTransport()
                .addProperty("configurationFile", "default-configs/default-jgroups-kubernetes.xml")
                .build();

        //Each node generates events, so we don't want to consume all memory available.
        //Let's limit number of entries to 100 and use sync mode
        ConfigurationBuilder cacheConfiguration = new ConfigurationBuilder();
        cacheConfiguration.clustering().cacheMode(CacheMode.DIST_SYNC);
        cacheConfiguration.eviction().strategy(EvictionStrategy.LRU).size(100);

        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, cacheConfiguration.build());
        Cache<String, String> cache = cacheManager.getCache();
        cache.addListener(new MyListener());

        //Each cluster member will update its own entry in the cache
        String hostname = Inet4Address.getLocalHost().getHostName();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> cache.put(hostname, Instant.now().toString()), 0, 2, TimeUnit.SECONDS);

        try {
            //This container will operate for an hour and then it will die
            TimeUnit.HOURS.sleep(1);
        } catch (InterruptedException e) {
            scheduler.shutdown();
            cacheManager.stop();
        }
    }

    @Listener
    public static class MyListener {

        @CacheEntryCreated
        public void entryCreated(CacheEntryCreatedEvent<String, String> event) {
            if (!event.isPre())
                System.out.println("Created new entry: " + event.getKey() + " " + event.getValue());
        }

        @CacheEntryModified
        public void entryModified(CacheEntryModifiedEvent<String, String> event) {
            if (!event.isPre())
                System.out.println("Updated entry: " + event.getKey() + " " + event.getValue());
        }
    }
}
