package org.infinispan.tutorial.simple.hibernate.cache.wildfly.local;

import org.hibernate.Session;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Event;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Person;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.logging.Logger;

/**
 * Start Wildfly, then:
 *
 * mvn clean package wildfly:deploy
 * curl http://localhost:8080/wildfly-local/infinispan/hibernate-cache/entity
 * curl http://localhost:8080/wildfly-local/infinispan/hibernate-cache/query
 * curl http://localhost:8080/wildfly-local/infinispan/hibernate-cache/expiring
 *
 */
@Path("/")
public class InfinispanHibernateCacheWildflyLocal {

   public static final String EVENT_REGION_NAME = "wildfly-local.war#events." + Event.class.getName();
   public static final String PERSON_REGION_NAME = "wildfly-local.war#events." + Person.class.getName();

   @Inject
   private Logger log;

   @Inject
   private EntityManager em;

   @Inject
   private UserTransaction userTransaction;

   @GET
   @Path("/hibernate-cache/entity")
   @Produces("application/json")
   public String entityCache() throws Exception {
      StringBuilder out = new StringBuilder();

      SecondLevelCacheStatistics eventCacheStats;

      // Persist 3 entities, stats should show 3 second level cache puts
      persistEntities();
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 3, out);

      // Find one of the persisted entities, stats should show a cache hit
      findEntity(1L, out);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(1L, out);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      // Find the updated entity, stats should show a cache hit
      findEntity(1L, out);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);


      // Evict entity from cache
      evictEntity(1L);

      // Reload evicted entity, should come from DB
      // Stats should show a cache miss and a cache put
      findEntity(1L, out);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache miss: %d (expected %d)%n", eventCacheStats.getMissCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      // Remove cached entity, stats should show a cache hit
      deleteEntity(1L);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/query")
   @Produces("application/json")
   public String queryCache() throws Exception {
      StringBuilder out = new StringBuilder();

      SecondLevelCacheStatistics eventCacheStats;
      Statistics stats;

      // Query entities, expect:
      // * no cache hits since query is not cached
      // * a query cache miss and query cache put
      queryEntities(out);
      stats = getStatistics();
      printfAssert("Query cache miss: %d (expected %d)%n", stats.getQueryCacheMissCount(), 1, out);
      printfAssert("Query cache put: %d (expected %d)%n", stats.getQueryCachePutCount(), 1, out);

      // Repeat query, expect:
      // * two cache hits for the number of entities in cache
      // * a query cache hit
      queryEntities(out);
      stats = getStatistics();
      printfAssert("Event entity cache hits: %d (expected %d)%n", stats.getSecondLevelCacheHitCount(), 2, out);
      printfAssert("Query cache hit: %d (expected %d)%n", stats.getQueryCacheHitCount(), 1, out);

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      updateEntity(2L, out);
      eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      // Repeat query after update, expect:
      // * no cache hits or puts since entities are already cached
      // * a query cache miss and query cache put, because when an entity is updated,
      //   any queries for that type are invalidated
      queryEntities(out);
      stats = getStatistics();
      printfAssert("Query cache miss: %d (expected %d)%n", stats.getQueryCacheMissCount(),1, out);
      printfAssert("Query cache put: %d (expected %d)%n", stats.getQueryCachePutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/expiring")
   @Produces("application/json")
   public String expiringEntityCache() throws Exception {
      StringBuilder out = new StringBuilder();

      SecondLevelCacheStatistics personCacheStats;
      
      // Save cache-expiring entity, stats should show a second level cache put
      saveExpiringEntity();
      personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache puts: %d (expected %d)%n", personCacheStats.getPutCount(), 1, out);

      // Find expiring entity, stats should show a second level cache hit
      findExpiringEntity(4L, out);
      personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache hits: %d (expected %d)%n", personCacheStats.getHitCount(), 1, out);

      // Wait long enough for entity to be expired from cache
      Thread.sleep(1100);

      // Find expiring entity, after expiration entity should come from DB
      // Stats should show a cache miss and a cache put
      findExpiringEntity(4L, out);
      personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache miss: %d (expected %d)%n", personCacheStats.getMissCount(), 1, out);
      printfAssert("Person entity cache put: %d (expected %d)%n", personCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   private void persistEntities() throws Exception {
      clearStatistics();
      userTransaction.begin();
      em.persist(new Event("Caught a pokemon!"));
      em.persist(new Event("Hatched an egg"));
      em.persist(new Event("Became a gym leader"));
      userTransaction.commit();
   }

   private void findEntity(long id, StringBuilder out) {
      clearStatistics();
      Event event = em.find(Event.class, id);
      out.append(String.format("Found entity: %s%n", event));
   }

   private void updateEntity(long id, StringBuilder out) throws Exception {
      clearStatistics();
      userTransaction.begin();
      Event event = em.find(Event.class, id);
      event.setName("Caught a Snorlax!!");
      out.append(String.format("Updated entity: %s%n", event));
      userTransaction.commit();
   }

   private void evictEntity(long id) {
      clearStatistics();
      em.getEntityManagerFactory().getCache().evict(Event.class, id);
   }

   private void deleteEntity(long id) throws Exception {
      clearStatistics();
      userTransaction.begin();
      Event event = em.find(Event.class, id);
      em.remove(event);
      userTransaction.commit();
   }

   private void queryEntities(StringBuilder out) {
      clearStatistics();
      log.info("Query entities");
      TypedQuery<Event> query = em.createQuery("from Event", Event.class);
      query.setHint("org.hibernate.cacheable", Boolean.TRUE);
      List<Event> events = query.getResultList();
      out.append(String.format("Queried events: %s%n", events));
   }

   private void saveExpiringEntity() throws Exception {
      clearStatistics();
      userTransaction.begin();
      em.persist(new Person("Satoshi"));
      userTransaction.commit();
   }

   private void findExpiringEntity(long id, StringBuilder out) {
      clearStatistics();
      Person person = em.find(Person.class, id);
      out.append(String.format("Found expiring entity: %s%n", person));
   }

   private SecondLevelCacheStatistics getCacheStatistics(String regionName) {
      return em.unwrap(Session.class).getSessionFactory().getStatistics()
         .getSecondLevelCacheStatistics(regionName);
   }

   private Statistics getStatistics() {
      return em.unwrap(Session.class).getSessionFactory().getStatistics();
   }

   private void clearStatistics() {
      em.unwrap(Session.class).getSessionFactory().getStatistics().clear();
   }

   private void printfAssert(String format, long actual, long expected, StringBuilder out) {
      String msg = String.format(format, actual, expected);
      out.append(msg);
      if (expected != actual) {
         String errorMsg = String.format("ERROR: Expected: %d, actual: %d%n", expected, actual);
         out.append(errorMsg);
         log.severe(errorMsg);
      }
   }

}
