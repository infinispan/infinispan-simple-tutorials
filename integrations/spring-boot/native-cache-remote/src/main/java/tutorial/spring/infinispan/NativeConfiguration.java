package tutorial.spring.infinispan;

import org.infinispan.protostream.GeneratedSchema;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import tutorial.spring.infinispan.model.AppSchemaImpl;

// tag::native[]
@Configuration
@ImportRuntimeHints(NativeConfiguration.InfinispanRuntimeHints.class)
public class NativeConfiguration {

   @Bean
   GeneratedSchema appSchema() {
      return new AppSchemaImpl();
   }

   static class InfinispanRuntimeHints implements RuntimeHintsRegistrar {

      @Override
      public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
         hints.resources().registerPattern("*.csv");
      }
   }
}
// end::native[]
