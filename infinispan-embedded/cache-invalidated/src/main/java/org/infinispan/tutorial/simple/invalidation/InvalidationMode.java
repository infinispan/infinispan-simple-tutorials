package org.infinispan.tutorial.simple.invalidation;

import java.util.Scanner;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;

/**
 * Invalidation mode tutorial showcases how an infinispan cluster configured in invalidation mode behaves. Run at least
 * two times this main. An Infinispan cluster will be formed and a menu will be displayed.
 * <p>
 * From the command line, you will be able to:
 *
 * <li>
 * <ul>`p`  - Put a key value</ul>
 * <ul>`pe` - Put a key value using the method {@link Cache#putForExternalRead(Object, Object)}</ul>
 * <ul>`e`  - Remove a key</ul>
 * <ul>`g`  - Get a key</ul>
 * <ul>`q`  - Exit</ul>
 * </li>
 *
 * <p>
 * In Invalidation mode, nodes don't share any data. Values are invalidated when a value exists in node A and it's
 * added/updated/removed in node B. If you want to avoid that invalidation when you use "put", you can use
 * "putForExternalRead". Note that your application could be using stale data in that case.
 * <p>
 * You can also use a special cache loader, {@link org.infinispan.persistence.cluster.ClusterLoader}. When ClusterLoader
 * is enabled, read operations that do not find the key on the local node will request it from all the other nodes
 * first, and store it in memory locally. This is not covered in this example, but you can give it a try using the
 * following configuration.
 * <code>
 * Configuration config = new ConfigurationBuilder().clustering().cacheMode(CacheMode.INVALIDATION_SYNC)
 * .persistence().addClusterLoader().remoteCallTimeout(500).build();
 * </code>
 * <p>
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class InvalidationMode {

   static DefaultCacheManager cacheManager;
   static Cache<String, String> cache;
   
   public static void main(String[] args) {
      createCacheManagerAndInitCache();

      Scanner scanner = new Scanner(System.in);
      String next = "";

      // Enter 'q' option to exit
      while (!next.equals("q")) {
         System.out.println(
               "Invalidation mode simple tutorial\n" +
                     "=================================\n" +
                     "`p` to put a key/value \n" +
                     "`pe` to put a key/value for external read \n" +
                     "`r` to remove a key \n" +
                     "`g` to get a key \n" +
                     "`q` to exit \n");
         System.out.println("Enter an option: ");
         next = scanner.next();

         switch (next) {
            case "p":
               putKeyValue(scanner, cache);
               break;
            case "pe":
               putForExternalReadKeyValue(scanner, cache);
               break;
            case "r":
               removeKey(scanner, cache);
               break;
            case "g":
               getKey(scanner, cache);
               break;

            default:
         }
      }

      // Goodbye!
      System.out.println("Good bye");

      stopCacheManager();
   }

   static void createCacheManagerAndInitCache() {
      // Create a clustered configuration and a default cache named test
      GlobalConfiguration global = GlobalConfigurationBuilder.defaultClusteredBuilder().build();

      // Create the cache manager
      cacheManager = new DefaultCacheManager(global);

      // Clustered mode invalidation sync. Can also be async
      Configuration config = new ConfigurationBuilder()
              .clustering().cacheMode(CacheMode.INVALIDATION_SYNC)
              .build();

      // Define a cache configuration
      cacheManager.defineConfiguration("test", config);

      // Retrieve the cache
      cache = cacheManager.getCache("test");
   }

   public static void stopCacheManager() {
      if (cacheManager != null) {
         // Stop the cache manager
         cacheManager.stop();
      }
   }

   static void putKeyValue(Scanner scanner, Cache<String, String> cache) {
      System.out.println("# p - Put key/value \n");
      String key = readUserInput("Enter a key: ", scanner);
      System.out.println("\n");
      String value = readUserInput("Enter a value: ", scanner);
      // Will put the key/value pair and invalidate the key/value pair that may exist in other nodes
      cache.put(key, value);
      System.out.println(String.format("[%s, %s] added", key, value));
   }

   static void putForExternalReadKeyValue(Scanner scanner, Cache<String, String> cache) {
      System.out.println("# pe - Put for external read key/value \n");
      String key = readUserInput("Enter a key: ", scanner);
      System.out.println("\n");
      String value = readUserInput("Enter a value: ", scanner);

      //Put for external read won't invalidate the value if it's present in another node.
      cache.putForExternalRead(key, value);
      System.out.println(String.format("[%s, %s] added for external read", key, value));
   }

   static void removeKey(Scanner scanner, Cache<String, String> cache) {
      System.out.println("# r - Remove key \n");
      String key = readUserInput("Enter a key: ", scanner);
      // Will remove the key/value pair if such exists and invalidate the key/value pair that may exist in other nodes
      cache.remove(key);
      System.out.println(String.format("%s key has been removed", key));
   }

   static void getKey(Scanner scanner, Cache<String, String> cache) {
      System.out.println("# g -> Get key \n");
      String key = readUserInput("Enter a key: ", scanner);
      String value = cache.get(key);
      System.out.println(String.format("%s key value is %s", key, value));
   }

   private static String readUserInput(String message, Scanner scanner) {
      System.out.println(message);
      return scanner.next();
   }
}
