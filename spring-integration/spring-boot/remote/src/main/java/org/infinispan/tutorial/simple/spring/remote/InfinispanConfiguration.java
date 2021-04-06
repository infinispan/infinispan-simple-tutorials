package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.infinispan.spring.starter.remote.InfinispanRemoteConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class InfinispanConfiguration {

   private final String XML = String.format(
         "<infinispan>"
               + "<cache-container>"
               + "   <distributed-cache name=\"" + Data.BASQUE_NAMES_CACHE + "\">"
               + "           <encoding>" + "               "
               + "               <key media-type=\"application/x-protostream\"/>"
               + "               <value media-type=\"application/x-protostream\"/>"
               + "            </encoding>"
               + "   </distributed-cache>"
               + "</cache-container>"
               + "</infinispan>", Data.BASQUE_NAMES_CACHE);

   @Bean
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public InfinispanRemoteCacheCustomizer caches() {
      return b -> {
         b.remoteCache(Data.BASQUE_NAMES_CACHE).marshaller(ProtoStreamMarshaller.class).configuration(XML);
         b.addContextInitializer(new SBSerializationContextInitializerImpl());
      };
   }
}
