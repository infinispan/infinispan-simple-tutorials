package org.infinispan.tutorial.simple.ai.spring.chatmemory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class InfinispanChatMemoryIT {

   @Container
   static InfinispanContainer infinispan = new InfinispanContainer();

   @DynamicPropertySource
   static void infinispanProperties(DynamicPropertyRegistry registry) {
      registry.add("infinispan.remote.server-list",
            () -> infinispan.getHost() + ":" + infinispan.getMappedPort(11222));
   }

   @Autowired
   ChatMemoryRepository chatMemoryRepository;

   @Test
   void saveAndRetrieveMessages() {
      String conversationId = "test-conversation-1";

      List<Message> messages = List.of(
            new UserMessage("What is Infinispan?"),
            AssistantMessage.builder()
                  .content("Infinispan is a distributed in-memory key/value data store.")
                  .build());

      chatMemoryRepository.saveAll(conversationId, messages);

      List<String> conversationIds = chatMemoryRepository.findConversationIds();
      assertThat(conversationIds).contains(conversationId);

      List<Message> retrieved = chatMemoryRepository.findByConversationId(conversationId);
      assertThat(retrieved).hasSize(2);
      assertThat(retrieved.get(0).getText()).isEqualTo("What is Infinispan?");
      assertThat(retrieved.get(1).getText()).contains("distributed in-memory");

      chatMemoryRepository.deleteByConversationId(conversationId);

      List<Message> afterDelete = chatMemoryRepository.findByConversationId(conversationId);
      assertThat(afterDelete).isEmpty();
   }
}
