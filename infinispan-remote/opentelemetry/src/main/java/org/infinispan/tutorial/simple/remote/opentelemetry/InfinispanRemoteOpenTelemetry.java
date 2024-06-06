package org.infinispan.tutorial.simple.remote.opentelemetry;

import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.concurrent.TimeUnit;

public class InfinispanRemoteOpenTelemetry {

   public static final String TRACING_CACHE_CONFIG =
           "<distributed-cache name=\"tracingCache\">\n"
                   + "    <encoding media-type=\"application/x-protostream\"/>\n"
                   + "    <tracing enabled=\"true\" categories=\"container cluster x-site persistence\"/>\n"
                   + "    <backups>\n"
                   + "        <backup site=\"NYC\" strategy=\"SYNC\" failure-policy=\"WARN\"/>\n"
                   + "    </backups>\n"
                   + "    <persistence passivation=\"true\">\n"
                   + "        <file-store shared=\"false\">\n"
                   + "           <data path=\"data\"/>\n"
                   + "           <index path=\"index\"/>\n"
                   + "           <write-behind modification-queue-size=\"2048\" />\n"
                   + "        </file-store>\n"
                   + "     </persistence>\n"
                   + "</distributed-cache>";

   public static void main(String[] args) throws InterruptedException {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      builder.remoteCache("tracingCache")
              .configuration(TRACING_CACHE_CONFIG);

      try (RemoteCacheManager client = TutorialsConnectorHelper.connect(builder)) {
         RemoteCache<String, String> cache = client.getCache("tracingCache");

         for (int i = 0; i < 100; i++) {
            cache.put("i" + i, i + "");
         }
         cache.remove("i0");
//         cache.clear();

         for (int i = 100; i < 200; i++) {
            cache.putIfAbsent("i" + i, i + "", 10, TimeUnit.SECONDS);
         }

         for (int i = 300; i < 400; i++) {
            cache.put("i" + i, i + "");
         }

         client.stop();
      }
   }

}
