package org.infinispan.tutorial.simple.counter;

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.counter.api.WeakCounter;
import org.infinispan.counter.configuration.CounterManagerConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

/**
 * The Counter simple tutorial.
 * <p>
 * The counters are available in Infinispan since version 9.1
 *
 * @author Pedro Ruivo
 */
public class InfinispanCounter {

   public static void main(String[] args) throws Exception {
      // Setup up a clustered cache manager
      GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
      // Create the counter configuration builder
      CounterManagerConfigurationBuilder builder = global.addModule(CounterManagerConfigurationBuilder.class);

      // Create 3 counters.
      // The first counter is bounded to 10 (upper-bound).
      builder.addStrongCounter().name("counter-1").upperBound(10).initialValue(1);
      // The second counter is unbounded
      builder.addStrongCounter().name("counter-2").initialValue(2);
      // And finally, the third counter is a weak counter.
      builder.addWeakCounter().name("counter-3").initialValue(3);
      // Initialize the cache manager
      DefaultCacheManager cacheManager = new DefaultCacheManager(global.build());

      // Retrieve the CounterManager from the CacheManager. Each CacheManager has it own CounterManager
      CounterManager counterManager = EmbeddedCounterManagerFactory.asCounterManager(cacheManager);

      // StrongCounter provides the higher consistency. Its value is known during the increment/decrement and it may be bounded.
      // Bounded counters are aimed for uses cases where a limit is needed.
      StrongCounter counter1 = counterManager.getStrongCounter("counter-1");
      // All methods returns a CompletableFuture. So you can do other work while the counter value is being computed.
      counter1.getValue().thenAccept(value -> System.out.println("Counter-1 initial value is " + value)).get();

      // Try to add more than the upper-bound
      counter1.addAndGet(10).handle((value, throwable) -> {
         // Value is null since the counter is bounded and we can add 10 to it.
         System.out.println("Counter-1 Exception is " + throwable.getMessage());
         return 0;
      }).get();

      // Check the counter value. It should be the upper-bound (10)
      counter1.getValue().thenAccept(value -> System.out.println("Counter-1 value is " + value)).get();

      //Decrement the value. Should be 9.
      counter1.decrementAndGet().handle((value, throwable) -> {
         // No exception this time.
         System.out.println("Counter-1 new value is " + value);
         return value;
      }).get();

      // Similar to counter-1, counter-2 is a strong counter but it is unbounded. It will never throw the CounterOutOfBoundsException
      StrongCounter counter2 = counterManager.getStrongCounter("counter-2");

      // All counters allow a listener to be registered.
      // The handle can be used to remove the listener
      counter2.addListener(event -> System.out
            .println("Counter-2 event: oldValue=" + event.getOldValue() + " newValue=" + event.getNewValue()));

      // Adding MAX_VALUE won't throws an exception. But the all the increments won't have any effect since we can store
      //any value larger the MAX_VALUE
      counter2.addAndGet(Long.MAX_VALUE).thenAccept(aLong -> System.out.println("Counter-2 value is " + aLong)).get();

      // Conditional operations are allowed in strong counters
      counter2.compareAndSet(Long.MAX_VALUE, 0)
            .thenAccept(aBoolean -> System.out.println("Counter-2 CAS result is " + aBoolean)).get();
      counter2.getValue().thenAccept(value -> System.out.println("Counter-2 value is " + value)).get();

      // Reset the counter to its initial value (2)
      counter2.reset().get();
      counter2.getValue().thenAccept(value -> System.out.println("Counter-2 initial value is " + value)).get();

      // Retrieve counter-3
      WeakCounter counter3 = counterManager.getWeakCounter("counter-3");
      // Weak counter doesn't have its value available during updates. This makes the increment faster than the StrongCounter
      // Its value is computed lazily and stored locally.
      // Its main use case is for uses-case where faster increments are needed.
      counter3.add(5).thenAccept(aVoid -> System.out.println("Adding 5 to counter-3 completed!")).get();

      // Check the counter value.
      System.out.println("Counter-3 value is " + counter3.getValue());

      // Stop the cache manager and release all resources
      cacheManager.stop();
   }

}
