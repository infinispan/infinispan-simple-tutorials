package org.infinispan.tutorial.simple.ai.spring.chatmemory;

import java.util.List;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatMemoryController {

   private final ChatMemoryRepository chatMemoryRepository;

   public ChatMemoryController(ChatMemoryRepository chatMemoryRepository) {
      this.chatMemoryRepository = chatMemoryRepository;
   }

   // tag::save[]
   @PostMapping(path = "/conversations/{conversationId}")
   public String addMessages(@PathVariable String conversationId,
         @RequestParam String userMessage,
         @RequestParam String assistantMessage) {
      List<Message> messages = List.of(
            new UserMessage(userMessage),
            AssistantMessage.builder().content(assistantMessage).build());
      chatMemoryRepository.saveAll(conversationId, messages);
      return "Saved " + messages.size() + " messages to conversation " + conversationId;
   }
   // end::save[]

   // tag::delete[]
   @DeleteMapping(path = "/conversations/{conversationId}")
   public String deleteConversation(@PathVariable String conversationId) {
      chatMemoryRepository.deleteByConversationId(conversationId);
      return "Deleted conversation " + conversationId;
   }
   // end::delete[]

   // tag::retrieve[]
   @GetMapping(path = "/conversations")
   public List<String> listConversations() {
      return chatMemoryRepository.findConversationIds();
   }

   @GetMapping(path = "/conversations/{conversationId}")
   public List<MessageResponse> getConversation(@PathVariable String conversationId) {
      return chatMemoryRepository.findByConversationId(conversationId).stream()
            .map(m -> new MessageResponse(m.getMessageType().name(), m.getText()))
            .toList();
   }
   // end::retrieve[]

   record MessageResponse(String type, String text) {
   }
}
