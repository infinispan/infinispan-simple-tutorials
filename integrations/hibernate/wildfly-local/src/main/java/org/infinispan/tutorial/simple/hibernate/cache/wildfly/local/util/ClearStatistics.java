package org.infinispan.tutorial.simple.hibernate.cache.wildfly.local.util;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

import org.hibernate.Session;

public class ClearStatistics {

   @Inject
   private EntityManager em;

   @AroundInvoke
   public Object clearStatistics(InvocationContext ic) throws Exception {
      em.unwrap(Session.class).getSessionFactory().getStatistics().clear();
      return ic.proceed();
   }

}
