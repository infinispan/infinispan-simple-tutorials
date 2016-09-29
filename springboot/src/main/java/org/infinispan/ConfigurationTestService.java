package org.infinispan;

import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Shows the configuration of infinispan cache manager on the system console.
 *
 * Created by Tomasz Zabłocki on 27.09.16.
 */
@Service
public class ConfigurationTestService {

    private final EmbeddedCacheManager cacheManager;

    @Autowired
    public ConfigurationTestService(EmbeddedCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void test(){
        String cacheName = "testDefaultCacheConfig";

        cacheManager.getCache(cacheName).put("testKey", "testValue");
        System.out.println("Received value from cache: " + cacheManager.getCache(cacheName).get("testKey"));

        System.out.println("====== CACHE CONFIGURATION =====");
        System.out.println("Cluster name: \n" + cacheManager.getClusterName());
        System.out.println("Cache names: \n" + cacheManager.getCacheNames());
        System.out.println("Default cache conf: \n" + cacheManager.getDefaultCacheConfiguration());
    }
}
