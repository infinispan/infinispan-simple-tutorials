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

      // Persist 3 entities, stats should show 3 second level cache puts
      SecondLevelCacheStatistics cacheStats = persistEntities(emf);
      System.out.printf("Event entity cache puts: %d (expected 3)%n", cacheStats.getPutCount());

      // Find one of the persisted entities, stats should show a cache hit
      cacheStats = findEntity(emf);
      System.out.printf("Event entity cache hits: %d (expected 1)%n", cacheStats.getHitCount());

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      cacheStats = updateEntity(1L, emf);
      System.out.printf("Event entity cache hits: %d (expected 2)%n", cacheStats.getHitCount());
      System.out.printf("Event entity cache puts: %d (expected 4)%n", cacheStats.getPutCount());

      // Find the updated entity, stats should show a cache hit
      cacheStats = findEntity(emf);
      System.out.printf("Event entity cache hits: %d (expected 3)%n", cacheStats.getHitCount());


      // Evict entity from cache
      evictEntity(emf);

      // Reload evicted entity, should come from DB
      // Stats should show a cache miss and a cache put
      cacheStats = findEntity(emf);
      System.out.printf("Event entity cache miss: %d (expected 1)%n", cacheStats.getMissCount());
      System.out.printf("Event entity cache puts: %d (expected 5)%n", cacheStats.getPutCount());

      // Remove cached entity, stats should show a cache hit
      cacheStats = deleteEntity(emf);
      System.out.printf("Event entity cache hits: %d (expected 4)%n", cacheStats.getHitCount());

      // Add a fictional delay so that there's a small enough gap for the
      // query result set timestamp to be later in time than the update timestamp.
      // Deleting an entity triggers an invalidation event which updates the timestamp.
      Thread.sleep(100);

      // Query entities, expect:
      // * no cache hits since query is not cached
      // * a query cache miss and query cache put
      Statistics stats = queryEntities(emf);
      System.out.printf("Query cache miss: %d (expected 1)%n", stats.getQueryCacheMissCount());
      System.out.printf("Query cache put: %d (expected 1)%n", stats.getQueryCachePutCount());

      // Repeat query, expect:
      // * two cache hits for the number of entities in cache
      // * a query cache hit
      stats = queryEntities(emf);
      System.out.printf("Event entity cache hits: %d (expected 6)%n", stats.getSecondLevelCacheHitCount());
      System.out.printf("Query cache hit: %d (expected 1)%n", stats.getQueryCacheHitCount());

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      cacheStats = updateEntity(2L, emf);
      System.out.printf("Event entity cache hits: %d (expected 7)%n", cacheStats.getHitCount());
      System.out.printf("Event entity cache puts: %d (expected 6)%n", cacheStats.getPutCount());

      // Repeat query after update, expect:
      // * no cache hits or puts since entities are already cached
      // * a query cache miss and query cache put, because when an entity is updated,
      //   any queries for that type are invalidated
      stats = queryEntities(emf);
      System.out.printf("Query cache miss: %d (expected 2)%n", stats.getQueryCacheMissCount());
      System.out.printf("Query cache put: %d (expected 2)%n", stats.getQueryCachePutCount());


      // Save cache-expiring entity, stats should show a second level cache put
      cacheStats = saveExpiringEntity(emf);
      System.out.printf("Person entity cache puts: %d (expected 1)%n", cacheStats.getPutCount());

      // Find expiring entity, stats should show a second level cache hit
      cacheStats = findExpiringEntity(emf);
      System.out.printf("Person entity cache hits: %d (expected 1)%n", cacheStats.getHitCount());

      // Wait long enough for entity to be expired from cache
      Thread.sleep(1100);

      // Find expiring entity, after expiration entity should come from DB
      // Stats should show a cache miss and a cache put
      cacheStats = findExpiringEntity(emf);
      System.out.printf("Person entity cache miss: %d (expected 1)%n", cacheStats.getMissCount());
      System.out.printf("Person entity cache put: %d (expected 2)%n", cacheStats.getPutCount());

      // Close persistence manager
      emf.close();
   }

   private static SecondLevelCacheStatistics persistEntities(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         em.persist(new Event("Caught a pokemon!"));
         em.persist(new Event("Hatched an egg"));
         em.persist(new Event("Became a gym leader"));

         return getCacheStatistics(Event.class.getName(), em);
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static SecondLevelCacheStatistics findEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         Event event = em.find(Event.class, 1L);
         System.out.printf("Found entity: %s%n", event);

         return getCacheStatistics(Event.class.getName(), em);
      } finally {
         em.close();
      }
   }

   private static SecondLevelCacheStatistics updateEntity(long id, EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         Event event = em.find(Event.class, id);
         String newName = "Caught a Snorlax!!";
         event.setName("Caught a Snorlax!!");

         System.out.printf("Updated entity: %s%n", event);

         return getCacheStatistics(Event.class.getName(), em);
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

   private static SecondLevelCacheStatistics deleteEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         Event event = em.find(Event.class, 1L);
         em.remove(event);

         return getCacheStatistics(Event.class.getName(), em);
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static Statistics queryEntities(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         TypedQuery<Event> query = em.createQuery("from Event", Event.class);
         query.setHint("org.hibernate.cacheable", Boolean.TRUE);
         List<Event> events = query.getResultList();
         System.out.printf("Queried events: %s%n", events);

         return getStatistics(em);
      } finally {
         em.close();
      }
   }

   private static SecondLevelCacheStatistics saveExpiringEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();

         em.persist(new Person("Satoshi"));

         return getCacheStatistics(Person.class.getName(), em);
      } catch (Throwable t) {
         tx.setRollbackOnly();
         throw t;
      } finally {
         if (tx.isActive()) tx.commit();
         else tx.rollback();

         em.close();
      }
   }

   private static SecondLevelCacheStatistics findExpiringEntity(EntityManagerFactory emf) {
      EntityManager em = emf.createEntityManager();
      try {
         Person person = em.find(Person.class, 4L);
         System.out.printf("Found expiring entity: %s%n", person);

         return getCacheStatistics(Person.class.getName(), em);
      } finally {
         em.close();
      }
   }

   private static SecondLevelCacheStatistics getCacheStatistics(
         String regionName, EntityManager em) {
      return ((Session) em.getDelegate())
            .getSessionFactory()
            .getStatistics()
            .getSecondLevelCacheStatistics(regionName);
   }

   private static Statistics getStatistics(EntityManager em) {
      return ((Session) em.getDelegate())
            .getSessionFactory()
            .getStatistics();
   }

}
