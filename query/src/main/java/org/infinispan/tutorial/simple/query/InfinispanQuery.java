package org.infinispan.tutorial.simple.query;

import java.util.List;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

public class InfinispanQuery {

   public static void main(String[] args) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.indexing().index(Index.ALL)
         .addProperty("default.directory_provider", "ram")
         .addProperty("lucene_version", "LUCENE_CURRENT");
      // Construct a simple local cache manager with default configuration
      DefaultCacheManager cacheManager = new DefaultCacheManager(builder.build());
      // Obtain the default cache
      Cache<String, Person> cache = cacheManager.getCache();
      // Store some entries
      cache.put("person1", new Person("William", "Shakespeare"));
      cache.put("person2", new Person("William", "Wordsworth"));
      cache.put("person3", new Person("John", "Milton"));
      // Obtain a query factory for the cache
      QueryFactory<?> queryFactory = Search.getQueryFactory(cache);
      // Construct a query
      Query query = queryFactory.from(Person.class).having("name").eq("William").toBuilder().build();
      // Execute the query
      List<Person> matches = query.list();
      // List the results
      matches.forEach(person -> System.out.printf("Match: %s", person));
      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
