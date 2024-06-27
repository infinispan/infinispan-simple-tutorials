package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.OS;
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

         // #### This is to avoid connectivity issues locally with docker and mac.
         // ### Don't use client intelligence basic in production
         if (OS.getCurrentOs().equals(OS.MAC_OS) || OS.getCurrentOs().equals(OS.WINDOWS)) {
            b.clientIntelligence(ClientIntelligence.BASIC);
         }
      };
   }
}
