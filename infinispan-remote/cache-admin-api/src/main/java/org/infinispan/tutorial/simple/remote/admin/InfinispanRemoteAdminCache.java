package org.infinispan.tutorial.simple.remote.admin;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The InfinispanRemoteAdminCache class shows how to remotely create caches
 * with Hot Rod Java clients using different approaches.
 * By default caches are permanent and survive cluster restarts.
 * To create volatile, temporary caches use "withFlags(AdminFlag.VOLATILE)".
 * Data in temporary caches is lost on full cluster restart.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf Dieter Fink</a>
 */
public class InfinispanRemoteAdminCache {
    private final RemoteCacheManager manager = TutorialsConnectorHelper.connect();

    public static void main(String[] args) throws Exception {
        InfinispanRemoteAdminCache client = new InfinispanRemoteAdminCache();

        client.createSimpleCache();
        client.cacheWithTemplate();
        client.createCacheWithXMLConfiguration();

        // Stop the Hot Rod client and release all resources.
        client.stop();
    }

    private void stop() {
        manager.stop();
    }

    /**
     * Creates a cache named CacheWithXMLConfiguration and uses the
     * XMLStringConfiguration() method to pass the cache definition as
     * valid infinispan.xml.
     */
    private void createCacheWithXMLConfiguration() throws IOException {
        String cacheName = "CacheWithXMLConfiguration";
        String xml = Files.readString(Paths.get(this.getClass().getClassLoader().getResource("CacheWithXMLConfiguration.xml").getPath()));
        manager.administration().getOrCreateCache(cacheName, new XMLStringConfiguration(xml));
        System.out.println("Cache with configuration exists or is created.");
    }

    /**
     * Creates a cache named CacheWithTemplate from the org.infinispan.DIST_SYNC
     * template.
     */
    private void cacheWithTemplate() throws IOException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource("cacheTemplate.xml").getPath());
        String xmlTemplate = Files.readString(path);
        try {
            manager.administration().createTemplate("template", new XMLStringConfiguration(xmlTemplate));
        } catch (Exception ce) {
            // If the
            System.out.println(ce.getMessage());
        }

        manager.administration().getOrCreateCache("CacheWithTemplate", "template");
        System.out.println("Cache created from default template.");
    }

    /**
     * Creates a simple, local cache with no configuration.
     */
    private void createSimpleCache() {
        try {
            manager.administration().createCache("SimpleCache", (String)null);
            System.out.println("SimpleCache created.");
        } catch (Exception e) {
            System.out.println("Expected to fail for multiple invocations as the cache exists message: " + e.getMessage());
        }
    }
}
