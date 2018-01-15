package org.infinispan.tutorial.simple.hibernate.cache.wildfly.local;

import org.hibernate.Session;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.controller.PersistenceManager;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Event;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Person;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.logging.Logger;

/**
 * Start Wildfly, then:
 *
 * mvn clean package wildfly:deploy
 * for i in {1..15}; do curl http://localhost:8080/wildfly-local/infinispan/hibernate-cache/$i; done
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
   private PersistenceManager ejb;

   @GET
   @Path("/hibernate-cache/1")
   @Produces("application/json")
   public String step1_persistEntities() {
      // Persist 3 entities, stats should show 3 second level cache puts
      ejb.persistEntities();
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      return printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 3);
   }

   @GET
   @Path("/hibernate-cache/2")
   @Produces("application/json")
   public String step2_findEntity() {
      StringBuilder out = new StringBuilder();

      // Find one of the persisted entities, stats should show a cache hit
      ejb.findEntity(1L, out);
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/3")
   @Produces("application/json")
   public String step3_updateEntity() {
      StringBuilder out = new StringBuilder();

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      ejb.updateEntity(1L, out);
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/4")
   @Produces("application/json")
   public String step4_findUpdatedEntity() {
      // Find the updated entity, stats should show a cache hit
      return step2_findEntity(); // Just repeat step 2
   }

   @GET
   @Path("/hibernate-cache/5")
   @Produces("application/json")
   public String step5_evictAndFindEntity() {
      StringBuilder out = new StringBuilder();

      // Evict entity from cache
      ejb.evictEntity(1L);

      // Reload evicted entity, should come from DB
      // Stats should show a cache miss and a cache put
      ejb.findEntity(1L, out);
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache miss: %d (expected %d)%n", eventCacheStats.getMissCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/6")
   @Produces("application/json")
   public String step6_deleteEntity() {
      StringBuilder out = new StringBuilder();

      // Remove cached entity, stats should show a cache hit
      ejb.deleteEntity(1L, out);
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/7")
   @Produces("application/json")
   public String step7_queryEntities() {
      StringBuilder out = new StringBuilder();

      // Query entities, expect:
      // * no cache hits since query is not cached
      // * a query cache miss and query cache put
      ejb.queryEntities(out);
      Statistics stats = getStatistics();
      printfAssert("Query cache miss: %d (expected %d)%n", stats.getQueryCacheMissCount(), 1, out);
      printfAssert("Query cache put: %d (expected %d)%n", stats.getQueryCachePutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/8")
   @Produces("application/json")
   public String step8_repeatQuery() {
      StringBuilder out = new StringBuilder();

      // Repeat query, expect:
      // * two cache hits for the number of entities in cache
      // * a query cache hit
      ejb.queryEntities(out);
      Statistics stats = getStatistics();
      printfAssert("Event entity cache hits: %d (expected %d)%n", stats.getSecondLevelCacheHitCount(), 2, out);
      printfAssert("Query cache hit: %d (expected %d)%n", stats.getQueryCacheHitCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/9")
   @Produces("application/json")
   public String step9_updateEntity() {
      StringBuilder out = new StringBuilder();

      // Update one of the persisted entities, stats should show a cache hit and a cache put
      ejb.updateEntity(2L, out);
      SecondLevelCacheStatistics eventCacheStats = getCacheStatistics(EVENT_REGION_NAME);
      printfAssert("Event entity cache hits: %d (expected %d)%n", eventCacheStats.getHitCount(), 1, out);
      printfAssert("Event entity cache puts: %d (expected %d)%n", eventCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/10")
   @Produces("application/json")
   public String step10_repeatQueryAfterUpdate() {
      // Repeat query after update, expect:
      // * no cache hits or puts since entities are already cached
      // * a query cache miss and query cache put, because when an entity is updated,
      //   any queries for that type are invalidated
      return step7_queryEntities();
   }

   @GET
   @Path("/hibernate-cache/11")
   @Produces("application/json")
   public String step11_persistExpiringEntity() {
      StringBuilder out = new StringBuilder();

      // Save cache-expiring entity, stats should show a second level cache put
      ejb.persistExpiringEntity();
      SecondLevelCacheStatistics personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache puts: %d (expected %d)%n", personCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/12")
   @Produces("application/json")
   public String step12_findExpiringEntity() {
      StringBuilder out = new StringBuilder();

      // Find expiring entity, stats should show a second level cache hit
      ejb.findExpiringEntity(4L, out);
      SecondLevelCacheStatistics personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache hits: %d (expected %d)%n", personCacheStats.getHitCount(), 1, out);

      return out.toString();
   }

   @GET
   @Path("/hibernate-cache/13")
   @Produces("application/json")
   public String step13_findExpiredEntity() throws Exception {
      StringBuilder out = new StringBuilder();

      // Wait long enough for entity to be expired from cache
      Thread.sleep(1100);

      // Find expiring entity, after expiration entity should come from DB
      // Stats should show a cache miss and a cache put
      ejb.findExpiringEntity(4L, out);
      SecondLevelCacheStatistics personCacheStats = getCacheStatistics(PERSON_REGION_NAME);
      printfAssert("Person entity cache miss: %d (expected %d)%n", personCacheStats.getMissCount(), 1, out);
      printfAssert("Person entity cache put: %d (expected %d)%n", personCacheStats.getPutCount(), 1, out);

      return out.toString();
   }

   private SecondLevelCacheStatistics getCacheStatistics(String regionName) {
      return em.unwrap(Session.class).getSessionFactory().getStatistics()
         .getSecondLevelCacheStatistics(regionName);
   }

   private Statistics getStatistics() {
      return em.unwrap(Session.class).getSessionFactory().getStatistics();
   }

   private void printfAssert(String format, long actual, long expected, StringBuilder out) {
      out.append(printfAssert(format, actual, expected));
   }

   private String printfAssert(String format, long actual, long expected) {
      StringBuilder out = new StringBuilder();
      String msg = String.format(format, actual, expected);
      out.append(msg);
      if (expected != actual) {
         String errorMsg = String.format("ERROR: Expected: %d, actual: %d%n", expected, actual);
         out.append(errorMsg);
         log.severe(errorMsg);
      }
      return out.toString();
   }

}
