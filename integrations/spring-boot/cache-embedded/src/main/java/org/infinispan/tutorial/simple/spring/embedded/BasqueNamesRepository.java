package org.infinispan.tutorial.simple.spring.embedded;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = Data.BASQUE_NAMES_CACHE)
public class BasqueNamesRepository {

   private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

   @Cacheable
   public BasqueName findById(int id) {
      logger.info("Call database to retrieve name by id '" + id + "'");
      return new BasqueName(id, Data.NAMES.get(id));
   }

}
