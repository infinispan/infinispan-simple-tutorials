package org.infinispan.tutorial.service;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.tutorial.model.Archetype;
import org.infinispan.tutorial.model.Character;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

@ApplicationScoped
public class DataLoader {
   public static final String CHARACTERS_CACHE = "characters";

   @ConfigProperty(name = "characters.filename")
   String charactersFileName;

   @Inject
   @Remote(CHARACTERS_CACHE)
   RemoteCache<String, Character> characters;

   void loadData(@Observes StartupEvent ev) {
       Log.info("On start - clean and load");
       try {
           loadCharacters();
           Log.infof("Characters loaded. Size: %s", characters.size());
       } catch (Exception e) {
           Log.error("Unable to load characters on startup", e);
       }
   }

   private void loadCharacters() throws Exception {
      InputStream resourceAsStream = this.getClass().getClassLoader()
            .getResourceAsStream(charactersFileName);

      try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
         String line;
         int id = 0;
         while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            int type = Integer.valueOf(values[0].trim());
            Archetype archetype = Archetype.values()[type];
            Character character = new Character(UUID.randomUUID(), values[1].trim(), values[2].trim(), archetype);
            characters.put(id + "", character);
            id++;
         }
      }
   }
}
