package org.infinispan.tutorial.simple.remote.counter;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.counter.api.CounterConfiguration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.CounterType;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.counter.api.WeakCounter;

/**
 * Remote Counter simple tutorial.
 * <p>
 * Remote counters are available as of Infinispan version 9.2.
 *
 * Infinispan Server includes a default property realm that requires
 * authentication. Create some credentials before you run this tutorial.
 *
 * @author Pedro Ruivo
 */
public class InfinispanRemoteCounter {

   public static void main(String[] args) throws Exception {
      // Create a configuration for a locally-running server
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer()
               .host("127.0.0.1")
               .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
             .security().authentication()
               //Add user credentials.
               .username("username")
               .password("password")
               .realm("default")
               .saslMechanism("DIGEST-MD5");

      // Connect to the server
      RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

      // Retrieve the CounterManager from the CacheManager. Each CacheManager has it own CounterManager
      CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(cacheManager);

      // Create 3 counters.
      // The first counter is bounded to 10 (upper-bound).
      counterManager.defineCounter("counter-1", CounterConfiguration.builder(CounterType.BOUNDED_STRONG)
            .upperBound(10)
            .initialValue(1)
            .build());

      // The second counter is unbounded
      counterManager.defineCounter("counter-2", CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG)
            .initialValue(2)
            .build());
      // And finally, the third counter is a weak counter.
      counterManager.defineCounter("counter-3", CounterConfiguration.builder(CounterType.WEAK)
            .initialValue(3)
            .build());

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
