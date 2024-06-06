package org.infinispan.tutorial.simple.remote.opentelemetry;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 * Shows how to enable tracing programatically in a cache, sends operations and traces
 * are displayed in Jaeger.
 *
 * Execute after running the docker-compose script in the docker-compose directory.
 */
public class InfinispanRemoteOpenTelemetry {
   public static void main(String[] args) {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      try (RemoteCacheManager client = TutorialsConnectorHelper.connect(builder)) {
         RemoteCache<String, String> cache = client.getCache(TUTORIAL_CACHE_NAME);

         // Enabled tracing at runtime by changing the configuration.
         client.administration()
                 .updateConfigurationAttribute(cache.getName(), "tracing.enabled", "true");

         for (int i = 0; i < 300; i++) {
            cache.put("i" + i, i + "");
         }

         client.stop();
      }
   }
}
