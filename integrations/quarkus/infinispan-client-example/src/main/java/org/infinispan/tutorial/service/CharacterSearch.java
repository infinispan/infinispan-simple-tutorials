package org.infinispan.tutorial.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.tutorial.model.Character;
import io.quarkus.infinispan.client.Remote;

@ApplicationScoped
public class CharacterSearch {
   @Inject
   @Remote(DataLoader.CHARACTERS_CACHE)
   RemoteCache<String, Character> characters;

   public Character getById(String id) {
      return characters.get(id);
   }

   public CompletionStage<Character> getByIdAsync(String id) {
      return characters.getAsync(id);
   }

   /**
    * Performs a simple full-text query on name and bio
    *
    * @param term
    * @return character names
    */
   public Set<String> search(String term) {
      if (characters == null) {
         Log.error("Unable to search...");
         throw new IllegalStateException("Characters store is null. Try restarting the application");
      }
      String query = "FROM tutorial.Character c"
      + " WHERE c.name:'~"+ term + "'"
      + " OR c.bio: '~" + term + "'";

      List<Character> result = characters.<Character>query(query).execute().list();
      return result.stream().map(Character::name).collect(Collectors.toSet());
   }

}
