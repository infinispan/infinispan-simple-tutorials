package org.infinispan.tutorial.simple.spring.remote;

import java.lang.invoke.MethodHandles;
import java.util.Random;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Reader {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final BasqueNamesRepository repository;
   private final Random random;
   private final RemoteCacheManager remoteCacheManager;

   public Reader(BasqueNamesRepository repository, RemoteCacheManager remoteCacheManager) {
      this.repository = repository;
      random = new Random();
      this.remoteCacheManager = remoteCacheManager;
      // Upload the generated schema in the server
      remoteCacheManager.administration().schemas().createOrUpdate(new BasquesNamesSchemaBuilderImpl());
   }

   @Scheduled(fixedDelay = 10000)
   public void retrieveSize() {
      logger.info(">>>> Cache size " + remoteCacheManager.getCache(Data.BASQUE_NAMES_CACHE).size());
   }

   @Scheduled(fixedDelay = 1000)
   public void retrieveBasqueName() {
      int id = this.random.nextInt(Data.NAMES.size());
      this.repository.findById(Integer.toString(id)).subscribe();
   }
}
