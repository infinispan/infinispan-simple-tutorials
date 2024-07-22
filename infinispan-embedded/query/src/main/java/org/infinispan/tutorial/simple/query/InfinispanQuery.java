package org.infinispan.tutorial.simple.query;

import java.util.List;

import org.infinispan.Cache;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.api.query.Query;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.IndexStorage;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanQuery {

   static Cache<String, Person> cache;
   static EmbeddedCacheManager cacheManager;

   public static void main(String[] args) throws Exception {
      createCacheManagerAndCache();
      List<Person> matches = addDataAndPerformQuery();
      // Display results
      matches.forEach(person -> System.out.printf("Match: %s%n", person));
      stopCacheManager();
   }

   static List<Person> addDataAndPerformQuery() {
      // Store some entries
      cache.put("person1", new Person("William", "Shakespeare"));
      cache.put("person2", new Person("William", "Wordsworth"));
      cache.put("person3", new Person("John", "Milton"));
      // Construct a query
      Query<Person> query = cache.query("from org.infinispan.tutorial.simple.query.Person where name = 'William'");
      // Execute the query
      return query.execute().list();
   }

   static EmbeddedCacheManager createCacheManagerAndCache() {
      // Create cache manager
      cacheManager = new DefaultCacheManager();
      // Create cache config
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.indexing()
              .enable()
              .storage(IndexStorage.LOCAL_HEAP)
              .addIndexedEntity(Person.class);

      // Obtain the cache
      cache = cacheManager.administration()
              .withFlags(CacheContainerAdmin.AdminFlag.VOLATILE)
              .getOrCreateCache("cache", builder.build());
      return cacheManager;
   }

   static void stopCacheManager() {
      if (cacheManager != null) {
         // Stop the cache manager and release all resources
         cacheManager.stop();
      }
   }
}
