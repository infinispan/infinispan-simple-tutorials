package org.infinispan.tutorial.simple.remote.admin;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.StringConfiguration;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The InfinispanRemoteAdminCache class shows how to remotely create caches
 * with Hot Rod Java clients using different approaches.
 * By default, caches are permanent and survive cluster restarts.
 * To create volatile, temporary caches use "withFlags(AdminFlag.VOLATILE)".
 * Data in temporary caches is lost on full cluster restart.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf Dieter Fink</a>
 */
public class InfinispanRemoteAdminCache {
    public static final String SIMPLE_CACHE = "SimpleCache";
    public static final String CACHE_WITH_XMLCONFIGURATION = "CacheWithXMLConfiguration";
    public static final String CACHE_WITH_TEMPLATE = "CacheWithTemplate";
    static RemoteCacheManager cacheManager = TutorialsConnectorHelper.connect();

    public static void main(String[] args) throws Exception {
        connectToInfinispan();

        createSimpleCache();
        cacheWithTemplate();
        createCacheWithXMLConfiguration();

        disconnect();
    }

    static void connectToInfinispan() {
        // Connect to the server
        cacheManager = TutorialsConnectorHelper.connect();
    }

    static void disconnect() {
        // Stop the cache manager and release all resources
        TutorialsConnectorHelper.stop(cacheManager);
    }

    /**
     * Creates a cache named CacheWithXMLConfiguration and uses the
     * StringConfiguration() method to pass the cache definition as
     * valid infinispan.xml.
     */
    static void createCacheWithXMLConfiguration() throws IOException {
        String xml = Files.readString(Paths.get(InfinispanRemoteAdminCache.class.getClassLoader().getResource("CacheWithXMLConfiguration.xml").getPath()));
        cacheManager.administration().getOrCreateCache(CACHE_WITH_XMLCONFIGURATION, new StringConfiguration(xml));
        System.out.println("Cache with configuration exists or is created.");
    }

    /**
     * Creates a cache named CacheWithTemplate from the org.infinispan.DIST_SYNC
     * template.
     */
    static void cacheWithTemplate() throws IOException {
        Path path = Paths.get(InfinispanRemoteAdminCache.class.getClassLoader().getResource("cacheTemplate.xml").getPath());
        String xmlTemplate = Files.readString(path);
        try {
            cacheManager.administration().createTemplate("template", new StringConfiguration(xmlTemplate));
        } catch (Exception ce) {
            // If the
            System.out.println(ce.getMessage());
        }

        cacheManager.administration().getOrCreateCache(CACHE_WITH_TEMPLATE, "template");
        System.out.println("Cache created from default template.");
    }

    /**
     * Creates a simple, local cache with no configuration.
     */
    static void createSimpleCache() {
        try {
            cacheManager.administration().createCache(SIMPLE_CACHE, (String)null);
            System.out.println("SimpleCache created.");
        } catch (Exception e) {
            System.out.println("Expected to fail for multiple invocations as the cache exists message: " + e.getMessage());
        }
    }
}
