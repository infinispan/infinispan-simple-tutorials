package org.infinispan.tutorial.simple.spring.remote;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@CacheConfig(cacheNames = Data.BASQUE_NAMES_CACHE)
public class BasqueNamesRepository {

   private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
   private static Map<String, BasqueName> database  = createDB();

   @Cacheable
   public Mono<BasqueName> findById(String id) {
      logger.info("Call database to FIND name by id '" + id + "'");
      return Mono.fromCallable(() -> database.get(id));
   }

   public int size() {
      return database.size();
   }

   private static Map<String, BasqueName> createDB() {
      Map<String, BasqueName> names = new HashMap<>();
      for (int i = 0; i < Data.NAMES.size(); i++) {
         String id = i + "";
         names.put(id, new BasqueName(id, Data.NAMES.get(i)));
      }
      return names;
   }

}
