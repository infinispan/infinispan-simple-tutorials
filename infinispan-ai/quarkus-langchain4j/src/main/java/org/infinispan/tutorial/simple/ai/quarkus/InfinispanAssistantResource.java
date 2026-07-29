package org.infinispan.tutorial.simple.ai.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// tag::resource[]
@Path("/chat")
public class InfinispanAssistantResource {

   @Inject
   InfinispanAssistant assistant;

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   public String chat(@QueryParam("question") String question) {
      return assistant.chat(question);
   }
}
// end::resource[]
