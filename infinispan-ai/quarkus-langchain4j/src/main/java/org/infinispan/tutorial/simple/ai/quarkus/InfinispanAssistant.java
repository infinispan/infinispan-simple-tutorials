package org.infinispan.tutorial.simple.ai.quarkus;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.RequestScoped;

// tag::ai-service[]
@RegisterAiService
@RequestScoped
public interface InfinispanAssistant {

   @SystemMessage("""
         You are an Infinispan expert assistant.
         Answer questions about Infinispan using the provided context.
         If you don't know the answer, say so.
         """)
   String chat(@UserMessage String question);
}
// end::ai-service[]
