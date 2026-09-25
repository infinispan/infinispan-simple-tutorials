package org.infinispan.tutorial.simple.ai.spring.vectorstore;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// tag::config[]
@Configuration(proxyBeanMethods = false)
public class Config {

   @Bean
   public EmbeddingModel embeddingModel() {
      return new TransformersEmbeddingModel();
   }
}
// end::config[]
