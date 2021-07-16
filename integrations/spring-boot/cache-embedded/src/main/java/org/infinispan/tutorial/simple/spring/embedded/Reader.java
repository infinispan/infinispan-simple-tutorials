package org.infinispan.tutorial.simple.spring.embedded;

import java.lang.invoke.MethodHandles;
import java.util.Random;

import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Reader {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final EmbeddedCacheManager cacheManager;
   private final BasqueNamesRepository repository;
   private final Random random;

   public Reader(BasqueNamesRepository repository, EmbeddedCacheManager embeddedCacheManager) {
      this.repository = repository;
      cacheManager = embeddedCacheManager;
      this.random = new Random();
   }

   @Scheduled(fixedDelay = 2000)
   public void retrieveSize() {
      logger.info("Cache size " + cacheManager.getCache(Data.BASQUE_NAMES_CACHE).size());
   }

   @Scheduled(fixedDelay = 1000)
   public void retrieveBasqueName() {
      int id = this.random.nextInt(Data.NAMES.size());
      logger.info("Find name by id '" + id + "'");
      this.repository.findById(id);
   }

}
