package tutorial.spring.infinispan.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.stereotype.Service;

import tutorial.spring.infinispan.model.Character;

// tag::service[]
@Service
public class CharacterSearch {

   private final RemoteCache<String, Character> characters;

   public CharacterSearch(RemoteCacheManager remoteCacheManager) {
      this.characters = remoteCacheManager.getCache("characters");
   }

   public Character getById(String id) {
      return characters.get(id);
   }

   public Set<String> search(String term) {
      String query = "FROM tutorial.Character c"
            + " WHERE c.name:'~" + term + "'"
            + " OR c.bio: '~" + term + "'";

      List<Character> result = characters.<Character>query(query).execute().list();
      return result.stream().map(Character::name).collect(Collectors.toSet());
   }
}
// end::service[]
