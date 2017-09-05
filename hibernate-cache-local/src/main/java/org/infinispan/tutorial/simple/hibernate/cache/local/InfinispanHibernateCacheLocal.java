package org.infinispan.tutorial.simple.hibernate.cache.local;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
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
      System.out.printf("Event entity cache puts: %d (expected 3)%n", eventCacheStats.getPutCount());

      // Find one of the persisted entities, stats should show a cache hit
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache hits: %d (expected 1)%n", eventCacheStats.getHitCount());

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache hits: %d (expected 2)%n", eventCacheStats.getHitCount());
      System.out.printf("Event entity cache puts: %d (expected 4)%n", eventCacheStats.getPutCount());

      // Find the updated entity, stats should show a cache hit
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache hits: %d (expected 3)%n", eventCacheStats.getHitCount());


      // Evict entity from cache
      evictEntity(emf);

      // Reload evicted entity, should come from DB
      // Stats should show a cache miss and a cache put
      findEntity(1L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache miss: %d (expected 1)%n", eventCacheStats.getMissCount());
      System.out.printf("Event entity cache puts: %d (expected 5)%n", eventCacheStats.getPutCount());

      // Remove cached entity, stats should show a cache hit
      deleteEntity(emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache hits: %d (expected 4)%n", eventCacheStats.getHitCount());

      // Add a fictional delay so that there's a small enough gap for the
      // query result set timestamp to be later in time than the update timestamp.
      // Deleting an entity triggers an invalidation event which updates the timestamp.
      Thread.sleep(100);

      // Query entities, expect:
      // * no cache hits since query is not cached
      // * a query cache miss and query cache put
      queryEntities(emf);
      stats = getStatistics(emf);
      System.out.printf("Query cache miss: %d (expected 1)%n", stats.getQueryCacheMissCount());
      System.out.printf("Query cache put: %d (expected 1)%n", stats.getQueryCachePutCount());

      // Repeat query, expect:
      // * two cache hits for the number of entities in cache
      // * a query cache hit
      queryEntities(emf);
      stats = getStatistics(emf);
      System.out.printf("Event entity cache hits: %d (expected 6)%n", stats.getSecondLevelCacheHitCount());
      System.out.printf("Query cache hit: %d (expected 1)%n", stats.getQueryCacheHitCount());

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(2L, emf);
      eventCacheStats = getCacheStatistics(Event.class.getName(), emf);
      System.out.printf("Event entity cache hits: %d (expected 7)%n", eventCacheStats.getHitCount());
      System.out.printf("Event entity cache puts: %d (expected 6)%n", eventCacheStats.getPutCount());

      // Repeat query after update, expect:
      // * no cache hits or puts since entities are already cached
      // * a query cache miss and query cache put, because when an entity is updated,
      //   any queries for that type are invalidated
      queryEntities(emf);
      stats = getStatistics(emf);
      System.out.printf("Query cache miss: %d (expected 2)%n", stats.getQueryCacheMissCount());
      System.out.printf("Query cache put: %d (expected 2)%n", stats.getQueryCachePutCount());


      // Save cache-expiring entity, stats should show a second level cache put
      saveExpiringEntity(emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      System.out.printf("Person entity cache puts: %d (expected 1)%n", personCacheStats.getPutCount());

      // Find expiring entity, stats should show a second level cache hit
      findExpiringEntity(emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      System.out.printf("Person entity cache hits: %d (expected 1)%n", personCacheStats.getHitCount());

      // Wait long enough for entity to be expired from cache
      Thread.sleep(1100);

      // Find expiring entity, after expiration entity should come from DB
      // Stats should show a cache miss and a cache put
      findExpiringEntity(emf);
      personCacheStats = getCacheStatistics(Person.class.getName(), emf);
      System.out.printf("Person entity cache miss: %d (expected 1)%n", personCacheStats.getMissCount());
      System.out.printf("Person entity cache put: %d (expected 2)%n", personCacheStats.getPutCount());

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

   private static void evictEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         em.getEntityManagerFactory().getCache().evict(Event.class, 1L);
      } finally {
         em.close();
      }
   }

   private static void deleteEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         Event event = em.find(Event.class, 1L);
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

   private static void findExpiringEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         Person person = em.find(Person.class, 4L);
         System.out.printf("Found expiring entity: %s%n", person);
      } finally {
         em.close();
      }
   }

   private static SecondLevelCacheStatistics getCacheStatistics(
         String regionName, EntityManagerFactory emf) {
      return ((Session) emf.createEntityManager().getDelegate())
            .getSessionFactory()
            .getStatistics()
            .getSecondLevelCacheStatistics(regionName);
   }

   private static Statistics getStatistics(EntityManagerFactory emf) {
      return ((Session) emf.createEntityManager().getDelegate())
            .getSessionFactory()
            .getStatistics();
   }

}
