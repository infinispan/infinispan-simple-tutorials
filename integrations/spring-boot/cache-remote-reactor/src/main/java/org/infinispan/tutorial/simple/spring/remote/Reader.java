package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Random;

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
      RemoteCache<String, String> metadataCache = this.remoteCacheManager.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
      GeneratedSchema schema = new BasquesNamesSchemaBuilderImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
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
