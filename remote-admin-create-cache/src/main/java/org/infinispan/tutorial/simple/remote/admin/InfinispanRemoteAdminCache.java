package org.infinispan.tutorial.simple.remote.admin;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin.AdminFlag;
import org.infinispan.commons.configuration.XMLStringConfiguration;

/**
 * The Administration simple tutorial to show how create caches with a remote
 * client.
 * There are some caches created by using different approach,
 * most of them are persistent and will survive a restart.
 * The temporary cache is also replicated across the cluster but lost on a full restart.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf Dieter Fink</a>
 */
public class InfinispanRemoteAdminCache {
    private final RemoteCacheManager manager;

    private InfinispanRemoteAdminCache() {
        // Create a configuration for a locally-running server
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host("127.0.0.1").port(ConfigurationProperties.DEFAULT_HOTROD_PORT);

        manager = new RemoteCacheManager(builder.build());
    }

    public static void main(String[] args) throws Exception {
        InfinispanRemoteAdminCache client = new InfinispanRemoteAdminCache();

        client.createSimpleCache();
        client.createCacheWithCacheConfiguration();
        client.createCacheWithXMLConfiguration();

        // Stop the client and release all resources
        client.stop();
    }

    private void stop() {
        manager.stop();
    }

    /**
     * For this a configuration is needed within the infinispan.xml file!
     * Add the following part to the <cache-container> element
     * <code>
     *    <distributed-cache-configuration name="MyDistCachecConfig" mode="ASYNC">
     *      <state-transfer await-initial-transfer="false"/>
     *     </distributed-cache-configuration>
     * </code>
     */
    private void createCacheWithCacheConfiguration() {
        manager.administration().getOrCreateCache("CacheWithConfigurationTemplate", "MyDistCachecConfig");
        System.out.println("Cache with template exists or is created");

        /* this cache is temporary and not persisted
         * to see whether the cache is created start another instance and see that a log message will shown
         * <code> [CLUSTER] [Context=TemporaryCacheWithConfigurationTemplate]ISPN100002: </code>
         */
        manager.administration().withFlags(AdminFlag.VOLATILE).getOrCreateCache("TemporaryCacheWithConfigurationTemplate", "MyDistCachecConfig");
        System.out.println("Temporary cache with template exists or is created");
    }

    /**
     * create a cache named CacheWithXMLConfiguration and provide the configuration
     * having XML by using an implementation of BasicConfiguration.
     * As result the cache is created and defaults are added.
     */
    private void createCacheWithXMLConfiguration() {
        String cacheName = "CacheWithXMLConfiguration";
        String xml = String.format("<infinispan>" +
                                      "<cache-container>" +
                                      "<distributed-cache name=\"%s\" mode=\"SYNC\">" +
                                        "<locking isolation=\"READ_COMMITTED\"/>" +
                                        "<transaction mode=\"NON_XA\"/>" +
                                        "<expiration lifespan=\"60000\" interval=\"20000\"/>" +
                                      "</distributed-cache>" +
                                      "</cache-container>" +
                                    "</infinispan>"
                                    , cacheName);
        manager.administration().getOrCreateCache(cacheName, new XMLStringConfiguration(xml));
        System.out.println("Cache with configuration exists or is created");
    }

    /**
     * create a simple cache without any configuration.
     * It will use the server defaults.
     */
    private void createSimpleCache() {
        try {
            manager.administration().createCache("SimpleCache", (String)null);
            System.out.println("SimpleCache created");
        } catch (Exception e) {
            System.out.println("Expected to fail for multiple invocations as the cache exists  message: " + e.getMessage());
        }
    }
}
