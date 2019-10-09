package org.infinispan;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;

import gnu.getopt.Getopt;

/**
 * Infinispan+operator+hotrod client quickstart
 *
 */
public class App {
    static String host = "127.0.0.1";
    static int port = ConfigurationProperties.DEFAULT_HOTROD_PORT;
    static String username, password;

    public static void main(String[] args) {
        // getopt and configure static variables
        configure(args);
        // Create a configuration for a locally-running server
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(host).port(port);
        if (username != null && password != null) {
            builder.security().authentication().saslMechanism("PLAIN").username(username).password(password);
        }
        // Connect to the server
        RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
        // Obtain the remote cache
        try {
        cacheManager.administration().createCache("quickstart-cache", "org.infinispan.DIST_SYNC");
        } catch (Exception ex) {
            // Maybe cache already exist? Go on anyway
        }
        RemoteCache<String, String> cache = cacheManager.getCache("quickstart-cache");
        /// Store a value
        cache.put("key", "value");
        // Retrieve the value and print it out
        System.out.printf("key = %s\n", cache.get("key"));
        // Stop the cache manager and release all resources
        cacheManager.administration().removeCache("quickstart-cache");
        cacheManager.stop();
    }

    static void configure(String args[]) {
        Getopt g = new Getopt("testprog", args, "h:p:U:P:");
        //
        int c;
        String arg;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'h':
                arg = g.getOptarg();
                host = arg;
                break;
            case 'p':
                arg = g.getOptarg();
                port = Integer.parseInt(arg);
                break;
            case 'U':
                arg = g.getOptarg();
                username = arg;
                break;
            case 'P':
                arg = g.getOptarg();
                password = arg;
                break;
            case '?':
            default:
                break;
            }
        }
    }
}
