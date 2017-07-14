package org.infinispan.tutorial.simple.functional;

import org.infinispan.AdvancedCache;
import org.infinispan.functional.EntryView;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.MetaParam.MetaLifespan;
import org.infinispan.functional.Traversable;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.infinispan.functional.impl.ReadWriteMapImpl;
import org.infinispan.functional.impl.WriteOnlyMapImpl;
import org.infinispan.manager.DefaultCacheManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InfinispanFunctional {

   public static void main(String[] args) throws Exception {
      DefaultCacheManager cacheManager = new DefaultCacheManager();
      cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
      AdvancedCache<String, String> cache = cacheManager.<String, String>getCache("local").getAdvancedCache();
      FunctionalMapImpl<String, String> functionalMap = FunctionalMapImpl.create(cache);
      FunctionalMap.WriteOnlyMap<String, String> writeOnlyMap = WriteOnlyMapImpl.create(functionalMap);
      FunctionalMap.ReadOnlyMap<String, String> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

      // Execute two parallel write-only operation to store key/value pairs
      CompletableFuture<Void> writeFuture1 = writeOnlyMap.eval("key1", "value1",
         (v, writeView) -> writeView.set(v));
      CompletableFuture<Void> writeFuture2 = writeOnlyMap.eval("key2", "value2",
         (v, writeView) -> writeView.set(v));

      // When each write-only operation completes, execute a read-only operation to retrieve the value
      CompletableFuture<String> readFuture1 =
         writeFuture1.thenCompose(r -> readOnlyMap.eval("key1", EntryView.ReadEntryView::get));
      CompletableFuture<String> readFuture2 =
         writeFuture2.thenCompose(r -> readOnlyMap.eval("key2", EntryView.ReadEntryView::get));

      // When the read-only operation completes, print it out
      System.out.printf("Created entries: %n");
      CompletableFuture<Void> end = readFuture1.thenAcceptBoth(readFuture2, (v1, v2) ->
         System.out.printf("key1 = %s%nkey2 = %s%n", v1, v2));

      // Wait for this read/write combination to finish
      end.get();

      // Create a read-write map
      FunctionalMap.ReadWriteMap<String, String> readWriteMap = ReadWriteMapImpl.create(functionalMap);

      // Use read-write multi-key based operation to write new values
      // together with lifespan and return previous values
      Map<String, String> data = new HashMap<>();
      data.put("key1", "newValue1");
      data.put("key2", "newValue2");
      Traversable<String> previousValues = readWriteMap.evalMany(data, (v, readWriteView) -> {
         String prev = readWriteView.find().orElse(null);
         readWriteView.set(v, new MetaLifespan(Duration.ofHours(1).toMillis()));
         return prev;
      });

      // Use read-only multi-key operation to read current values for multiple keys
      Traversable<EntryView.ReadEntryView<String, String>> entryViews =
         readOnlyMap.evalMany(data.keySet(), readOnlyView -> readOnlyView);
      System.out.printf("Updated entries: %n");
      entryViews.forEach(view -> System.out.printf("%s%n", view));

      // Finally, print out the previous entry values
      System.out.printf("Previous entry values: %n");
      previousValues.forEach(prev -> System.out.printf("%s%n", prev));
   }

}
