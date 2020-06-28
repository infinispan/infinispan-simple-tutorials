package org.infinispan.tutorial.simple.spring.remote;

import java.lang.invoke.MethodHandles;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Reader {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final BasqueNamesRepository repository;
   private final Random random;

   // Manipulate the underlying cache to show some messages
   private RemoteCache<Integer, String> cache;

   private final String XML = String.format("<infinispan><cache-container><distributed-cache name=\"" + Data.BASQUE_NAMES_CACHE + "\"></distributed-cache></cache-container></infinispan>", "cache");

   public Reader(BasqueNamesRepository repository, RemoteCacheManager remoteCacheManager) {
      this.repository = repository;
      random = new Random();
      cache = remoteCacheManager.administration().getOrCreateCache(Data.BASQUE_NAMES_CACHE, new XMLStringConfiguration(XML));
      try {
         cache.clearAsync().get(1, TimeUnit.MINUTES);
      } catch (Exception e) {
         logger.error("Unable to clear the cache", e);
      }
   }

   @Scheduled(fixedDelay = 10000)
   public void retrieveSize() {
      logger.info(">>>> Cache size " + cache.size());
      logger.info(">>>> Database size " + repository.size());
   }

   @Scheduled(fixedDelay = 1000)
   public void createOne() {
      int id = this.random.nextInt(Data.NAMES.size());
      this.repository.create(id, Data.NAMES.get(id));
   }

   @Scheduled(fixedDelay = 3000)
   public void removeOne() {
      int id = this.random.nextInt(Data.NAMES.size());
      this.repository.removeById(id);
   }

   @Scheduled(fixedDelay = 1000)
   public void retrieveBasqueName() {
      int id = this.random.nextInt(Data.NAMES.size());
      logger.info("FIND RESULT " + this.repository.findById(id));
   }
}
