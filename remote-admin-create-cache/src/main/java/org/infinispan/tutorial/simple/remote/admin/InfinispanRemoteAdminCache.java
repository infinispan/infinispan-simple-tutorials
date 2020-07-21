package org.infinispan.tutorial.simple.remote.admin;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin.AdminFlag;
import org.infinispan.commons.configuration.XMLStringConfiguration;

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
    private final RemoteCacheManager manager;

    private InfinispanRemoteAdminCache() {
        // Create a configuration for a locally running server.
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                 .host("127.0.0.1")
                 .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
               .security().authentication()
                 //Add user credentials.
                 .username("username")
                 .password("password")
                 .realm("default")
                 .saslMechanism("DIGEST-MD5");

        manager = new RemoteCacheManager(builder.build());
    }

    public static void main(String[] args) throws Exception {
        InfinispanRemoteAdminCache client = new InfinispanRemoteAdminCache();

        client.createSimpleCache();
        client.CacheWithTemplate();
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
    private void createCacheWithXMLConfiguration() {
        String cacheName = "CacheWithXMLConfiguration";
        String xml = String.format("<infinispan>" +
                                      "<cache-container>" +
                                      "<distributed-cache name=\"%s\" mode=\"SYNC\">" +
                                        "<encoding media-type=\"application/x-protostream\"/>" +
                                        "<locking isolation=\"READ_COMMITTED\"/>" +
                                        "<transaction mode=\"NON_XA\"/>" +
                                        "<expiration lifespan=\"60000\" interval=\"20000\"/>" +
                                      "</distributed-cache>" +
                                      "</cache-container>" +
                                    "</infinispan>"
                                    , cacheName);
        manager.administration().getOrCreateCache(cacheName, new XMLStringConfiguration(xml));
        System.out.println("Cache with configuration exists or is created.");
    }

    /**
     * Creates a cache named CacheWithTemplate from the org.infinispan.DIST_SYNC
     * template.
     */
    private void CacheWithTemplate() {
        manager.administration().getOrCreateCache("CacheWithTemplate", DefaultTemplate.DIST_SYNC);
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
