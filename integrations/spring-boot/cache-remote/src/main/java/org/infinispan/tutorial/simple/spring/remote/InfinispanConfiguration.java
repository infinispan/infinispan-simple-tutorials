package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.spring.starter.remote.InfinispanRemoteCacheCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class InfinispanConfiguration {

   @Bean
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public InfinispanRemoteCacheCustomizer caches() {
      return b -> {
         URI cacheConfigUri;
         try {
            cacheConfigUri = this.getClass().getClassLoader().getResource("basquesNamesCache.xml").toURI();
         } catch (URISyntaxException e) {
            throw new RuntimeException(e);
         }

         b.remoteCache(Data.BASQUE_NAMES_CACHE)
                 .configurationURI(cacheConfigUri);

         b.remoteCache(Data.BASQUE_NAMES_CACHE).marshaller(ProtoStreamMarshaller.class);

         // Add marshaller in the client, the class is generated from the interface in compile time
         b.addContextInitializer(new BasquesNamesSchemaBuilderImpl());
      };
   }
}
