package tutorial.spring.infinispan;

import java.util.Collections;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tutorial.spring.infinispan.model.Character;
import tutorial.spring.infinispan.service.CharacterSearch;

// tag::resource[]
@RestController
@RequestMapping("/characters")
public class CharactersResource {

   private final CharacterSearch searchService;

   public CharactersResource(CharacterSearch searchService) {
      this.searchService = searchService;
   }

   @GetMapping("/{id}")
   public Character byId(@PathVariable("id") String id) {
      Character character = searchService.getById(id);
      if (character == null) {
         throw new ResponseStatusException(HttpStatus.NOT_FOUND,
               "Character with id of " + id + " does not exist.");
      }
      return character;
   }

   @GetMapping("/query")
   public Set<String> searchCharacter(@RequestParam(value = "term", required = false) String term) {
      if (term == null) {
         return Collections.emptySet();
      }
      return searchService.search(term);
   }
}
// end::resource[]
