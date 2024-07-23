package org.infinispan.tutorial.simple.lock;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.lock.EmbeddedClusteredLockManagerFactory;
import org.infinispan.lock.api.ClusteredLock;
import org.infinispan.lock.api.ClusteredLockManager;
import org.infinispan.manager.DefaultCacheManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class InfinispanClusteredLock {

   static DefaultCacheManager cacheManager;
   static ClusteredLockManager clusteredLockManager;
   static ClusteredLock lock;
   static AtomicInteger counter;

   public static void main(String[] args) throws Exception {
      createAndStartComponents();
      changeCounterWithLocks();
      stop();
   }

   static void changeCounterWithLocks() throws InterruptedException, ExecutionException, TimeoutException {
      // Acquire and release the lock 3 times
      CompletableFuture<Boolean> call1 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 1");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 1");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture<Boolean> call2 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 2");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 2");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture<Boolean> call3 = lock.tryLock(1, TimeUnit.SECONDS).whenComplete((r, ex) -> {
         if (r) {
            System.out.println("lock is acquired by the call 3");
            lock.unlock().whenComplete((nil, ex2) -> {
               System.out.println("lock is released by the call 3");
               counter.incrementAndGet();
            });
         }
      });

      CompletableFuture.allOf(call1, call2, call3).whenComplete((r, ex) -> {
         // Print the value of the counter
         System.out.println("Value of the counter is " + counter.get());
      }).get(10, TimeUnit.SECONDS);
   }

   static void createAndStartComponents() {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

      // Initialize 1 cache managers
      cacheManager = new DefaultCacheManager(global.build());

      // Initialize the clustered lock manager from the cache manager
      clusteredLockManager = EmbeddedClusteredLockManagerFactory.from(cacheManager);

      // Define a lock. By default, this lock is non reentrant
      clusteredLockManager.defineLock("lock");

      // Get a lock interface from each node
      lock = clusteredLockManager.get("lock");

      counter = new AtomicInteger(0);
   }

   static void stop() {
      if (cacheManager != null) {
         // Stop the cache manager
         cacheManager.stop();
      }
   }
}
