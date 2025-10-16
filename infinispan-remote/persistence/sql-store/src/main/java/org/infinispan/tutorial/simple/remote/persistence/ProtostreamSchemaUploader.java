package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.client.hotrod.RemoteCacheManager;

public class ProtostreamSchemaUploader {

   public static final TechLibrarySchemaImpl TECH_LIBRARY_SCHEMA = new TechLibrarySchemaImpl();
   private final RemoteCacheManager cacheManager;

   public ProtostreamSchemaUploader(RemoteCacheManager cacheManager) {
      this.cacheManager = cacheManager;
   }

   public void registerSchema() {
      cacheManager.administration().schemas().createOrUpdate(TECH_LIBRARY_SCHEMA);
   }

   public void unregisterSchema() {
      cacheManager.administration().schemas().remove(TECH_LIBRARY_SCHEMA.getProtoFileName());
   }
}
