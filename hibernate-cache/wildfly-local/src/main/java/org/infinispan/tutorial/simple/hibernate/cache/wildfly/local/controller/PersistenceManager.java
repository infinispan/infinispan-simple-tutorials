package org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.controller;

import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Event;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.model.Person;
import org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.util.ClearStatistics;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
@Interceptors(ClearStatistics.class)
public class PersistenceManager {

   @Inject
   private EntityManager em;

   public void persistEntities() {
      em.persist(new Event("Caught a pokemon!"));
      em.persist(new Event("Hatched an egg"));
      em.persist(new Event("Became a gym leader"));
   }

   public void findEntity(long id, StringBuilder out) {
      Event event = em.find(Event.class, id);
      out.append(String.format("Found entity: %s%n", event));
   }

   public void updateEntity(long id, StringBuilder out) {
      Event event = em.find(Event.class, id);
      event.setName("Caught a Snorlax!!");
      out.append(String.format("Updated entity: %s%n", event));
   }

   public void deleteEntity(long id, StringBuilder out) {
      Event event = em.find(Event.class, id);
      em.remove(event);
      out.append(String.format("Removed entity: %s%n", event));
   }

   public void evictEntity(long id) {
      em.getEntityManagerFactory().getCache().evict(Event.class, id);
   }

   public void persistExpiringEntity() {
      em.persist(new Person("Satoshi"));
   }

   public void queryEntities(StringBuilder out) {
      TypedQuery<Event> query = em.createQuery("from Event", Event.class);
      query.setHint("org.hibernate.cacheable", Boolean.TRUE);
      List<Event> events = query.getResultList();
      out.append(String.format("Queried events: %s%n", events));
   }

   public void findExpiringEntity(long id, StringBuilder out) {
      Person person = em.find(Person.class, id);
      out.append(String.format("Found expiring entity: %s%n", person));
   }

}
