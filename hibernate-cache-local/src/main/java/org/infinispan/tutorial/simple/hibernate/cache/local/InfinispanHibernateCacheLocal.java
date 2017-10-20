package org.infinispan.tutorial.simple.hibernate.cache.local;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.infinispan.tutorial.simple.hibernate.cache.local.model.Event;
import org.infinispan.tutorial.simple.hibernate.cache.local.model.Person;

/**
 * Example on how to use Infinispan as Hibernate cache provider
 * in a standalone, single-node, environment.
 *
 * This example assumes that the JPA persistence configuration file
 * is present in the default lookup location: META-INF/persistence.xml
 *
 * Run with these properties to hide Hibernate messages:
 * -Dlog4j.configurationFile=src/main/resources/log4j2-tutorial.xml
 *
 * Run with -ea to enable assertions and verify expected behaviour.
 */
public class InfinispanHibernateCacheLocal {

   public static void main(String[] args) throws Exception {
      // Create JPA persistence manager
      EntityManagerFactory emf = Persistence.createEntityManagerFactory("events");

      SecondLevelCacheStatistics eventCacheStats;
      SecondLevelCacheStatistics personCacheStats;
      Statistics stats;

      // Persist 3 entities, stats should show 3 second level cache puts
      persistEntities(emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 3);

      // Find one of the persisted entities, stats should show a cache hit
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1);

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 2);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 4);

      // Find the updated entity, stats should show a cache hit
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 3);


      // Evict entity from cache
      evictEntity(1L, emf);

      // Reload evicted entity, should come from DB
      // Stats should show a cache miss and a cache put
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache miss: %d (expected %d)%n", eventCacheStats.getMissCount(), 1);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 5);

      // Remove cached entity, stats should show a cache hit
      deleteEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 4);

      // Add a fictional delay so that there's a small enough gap for the
      // query result set timestamp to be later in time than the update timestamp.
      // Deleting an entity triggers an invalidation event which updates the timestamp.
      Thread.sleep(100);

      // Query entities, expect:
      // * no cache hits since query is not cached
      // * a query cache miss and query cache put
      queryEntities(emf);
      stats = getStatistics(emf);
      printfAssert("Query cache miss: %d (expected %d)%n", stats.getQueryCacheMissCount(), 1);
      printfAssert("Query cache put: %d (expected %d)%n", stats.getQueryCachePutCount(), 1);

      // Repeat query, expect:
      // * two cache hits for the number of entities in cache
      // * a query cache hit
      queryEntities(emf);
      stats = getStatistics(emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", stats.getSecondLevelCacheHitCount(), 6);
      printfAssert("Query cache hit: %d (expected %d)%n", stats.getQueryCacheHitCount(), 1);

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(2L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 7);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 6);

      // Repeat query after update, expect:
      // * no cache hits or puts since entities are already cached
      // * a query cache miss and query cache put, because when an entity is updated,
      //   any queries for that type are invalidated
      queryEntities(emf);
      stats = getStatistics(emf);
      printfAssert("Query cache miss: %d (expected %d)%n", stats.getQueryCacheMissCount(),2);
      printfAssert("Query cache put: %d (expected %d)%n", stats.getQueryCachePutCount(), 2);


      // Save cache-expiring entity, stats should show a second level cache put
      saveExpiringEntity(emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      printfAssert("Person entity cache puts: %d (expected %d)%n", personCacheStats.getPutCount(), 1);

      // Find expiring entity, stats should show a second level cache hit
      findExpiringEntity(4L, emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      printfAssert("Person entity cache hits: %d (expected %d)%n", personCacheStats.getHitCount(), 1);

      // Wait long enough for entity to be expired from cache
      Thread.sleep(1100);

      // Find expiring entity, after expiration entity should come from DB
      // Stats should show a cache miss and a cache put
      findExpiringEntity(4L, emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      printfAssert("Person entity cache miss: %d (expected %d)%n", personCacheStats.getMissCount(), 1);
      printfAssert("Person entity cache put: %d (expected %d)%n", personCacheStats.getPutCount(), 2);

      // Close persistence manager
      emf.close();
   }

   private static void persistEntities(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         em.persist(new Event("Caught a pokemon!"));
         em.persist(new Event("Hatched an egg"));
         em.persist(new Event("Became a gym leader"));
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static void findEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         Event event = em.find(Event.class, id);
         System.out.printf("Found entity: %s%n", event);
      } finally {
         em.close();
      }
   }

   private static void updateEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         Event event = em.find(Event.class, id);
         event.setName("Caught a Snorlax!!");

         System.out.printf("Updated entity: %s%n", event);
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static void evictEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         em.getEntityManagerFactory().getCache().evict(Event.class, id);
      } finally {
         em.close();
      }
   }

   private static void deleteEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         Event event = em.find(Event.class, id);
         em.remove(event);
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static void queryEntities(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         TypedQuery<Event> query = em.createQuery("from Event", Event.class);
         query.setHint("org.hibernate.cacheable", Boolean.TRUE);
         List<Event> events = query.getResultList();
         System.out.printf("Queried events: %s%n", events);
      } finally {
         em.close();
      }
   }

   private static void saveExpiringEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         em.persist(new Person("Satoshi"));
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static void findExpiringEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         Person person = em.find(Person.class, id);
         System.out.printf("Found expiring entity: %s%n", person);
      } finally {
         em.close();
      }
   }

   private static SecondLevelCacheStatistics getCacheStatistics(
         String regionName, EntityManagerFactory emf) {
      return emf.unwrap(SessionFactory.class).getStatistics()
            .getSecondLevelCacheStatistics(regionName);
   }

   private static Statistics getStatistics(EntityManagerFactory emf) {
      return emf.unwrap(SessionFactory.class).getStatistics();
   }

   private static void printfAssert(String format, long actual, int expected) {
      System.out.printf(format, actual, expected);
      assert expected == actual : "Expected: " + expected + ", actual: " + actual;
   }

}
