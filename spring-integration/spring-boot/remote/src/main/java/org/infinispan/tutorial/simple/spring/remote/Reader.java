package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Random;

@Component
public class Reader {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final BasqueNamesRepository repository;
   private final Random random;

   // Manipulate the underlying cache to show some messages
   private RemoteCache<Integer, BasqueName> cache;

   @Autowired
   public Reader(BasqueNamesRepository repository, RemoteCacheManager remoteCacheManager) {
      this.repository = repository;
      random = new Random();
      cache = remoteCacheManager.getCache(Data.BASQUE_NAMES_CACHE);
      cache.clearAsync().exceptionally(e -> {
         logger.error("Unable to clear the cache", e);
         return null;
      });
      RemoteCache<String, String> metadataCache =
           remoteCacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
      GeneratedSchema schema = new SBSerializationContextInitializerImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
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
