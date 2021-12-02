package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.protostream.GeneratedSchema;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

public class ProtostreamSchemaUploader {

   private final RemoteCacheManager cacheManager;

   public ProtostreamSchemaUploader(RemoteCacheManager cacheManager) {
      this.cacheManager = cacheManager;
   }

   public void registerSchema() {
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);
      GeneratedSchema schema = new TechLibrarySchemaImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
   }

   public void unregisterSchema() {
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);
      GeneratedSchema schema = new TechLibrarySchemaImpl();
      metadataCache.remove(schema.getProtoFileName());
   }
}
