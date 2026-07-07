package tutorial.spring.infinispan.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import tutorial.spring.infinispan.model.Archetype;
import tutorial.spring.infinispan.model.Character;

@Component
public class DataLoader {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final RemoteCacheManager remoteCacheManager;

   @Value("${characters.filename}")
   private String charactersFileName;

   public DataLoader(RemoteCacheManager remoteCacheManager) {
      this.remoteCacheManager = remoteCacheManager;
   }

   @EventListener(ApplicationReadyEvent.class)
   public void loadData() {
      logger.info("On start - clean and load");
      try {
         loadCharacters();
      } catch (Exception e) {
         logger.error("Unable to load characters on startup", e);
      }
   }

   private void loadCharacters() throws Exception {
      RemoteCache<String, Character> characters =
            remoteCacheManager.getCache("characters");

      InputStream resourceAsStream = this.getClass().getClassLoader()
            .getResourceAsStream(charactersFileName);

      try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
         String line;
         int id = 0;
         while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            int type = Integer.parseInt(values[0].trim());
            Archetype archetype = Archetype.values()[type];
            Character character = new Character(UUID.randomUUID(), values[1].trim(), values[2].trim(), archetype);
            characters.put(String.valueOf(id), character);
            id++;
         }
      }

      logger.info("Characters loaded. Size: {}", characters.size());
   }
}
