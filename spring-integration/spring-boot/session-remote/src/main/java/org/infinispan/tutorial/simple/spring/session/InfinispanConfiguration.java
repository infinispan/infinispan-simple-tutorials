package org.infinispan.tutorial.simple.spring.session;

import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class InfinispanConfiguration {

   @Bean
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public InfinispanRemoteCacheCustomizer caches() {
      return b -> {
         // Ask the server to create this cache on startup
         b.remoteCache("sessions")
                 .configuration("<distributed-cache name=\"sessions\"><encoding media-type=\"application/x-protostream\"/></distributed-cache>");

         // Use protostream marshaller to serialize the sessions with Protobuf
         b.remoteCache("sessions").marshaller(ProtoStreamMarshaller.class);
      };
   }
}
