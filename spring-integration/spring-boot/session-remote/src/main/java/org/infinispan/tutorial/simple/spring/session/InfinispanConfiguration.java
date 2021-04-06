package org.infinispan.tutorial.simple.spring.session;

import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class InfinispanConfiguration {

   @Bean
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public InfinispanRemoteCacheCustomizer sessionCache() {
      String xml = String.format(""
            + "<infinispan>" + "   "
            + "   <cache-container>"
            + "       <distributed-cache name=\"%s\" mode=\"SYNC\" owners=\"1\" statistics=\"true\">"
            + "           <encoding>" + "               "
            + "               <key media-type=\"application/x-java-serialized-object\"/>"
            + "               <value media-type=\"application/x-java-serialized-object\"/>"
            + "            </encoding>"
            + "       </distributed-cache>"
            + "   </cache-container>"
            + "</infinispan>"
            , "sessions");
      return b -> {
         b.remoteCache("sessions").configuration(xml);
      };

   }
}
