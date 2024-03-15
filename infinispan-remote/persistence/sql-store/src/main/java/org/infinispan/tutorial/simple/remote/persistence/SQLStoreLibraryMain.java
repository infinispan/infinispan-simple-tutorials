package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.net.URI;
import java.util.Scanner;

public class SQLStoreLibraryMain {

   public static final String AUTHORS_CACHE = "authors-cache";
   public static final String BOOKS_CACHE = "books-cache";

   public static void main(String[] args) throws Exception {
      DBCreator dbCreator = new DBCreator();
      dbCreator.startDBServer("9123");
      dbCreator.createAndPopulate();

      ConfigurationBuilder configurationBuilder = TutorialsConnectorHelper.connectionConfig();
      configurationBuilder.addContextInitializer(new TechLibrarySchemaImpl());
      URI authorsCacheURI = SQLStoreLibraryMain.class.getClassLoader().getResource("sqlTableCache.xml").toURI();
      URI booksCacheURI = SQLStoreLibraryMain.class.getClassLoader().getResource("sqlQueryCache.xml").toURI();
      configurationBuilder.remoteCache(AUTHORS_CACHE).configurationURI(authorsCacheURI);
      configurationBuilder.remoteCache(BOOKS_CACHE).configurationURI(booksCacheURI);

      RemoteCacheManager cacheManager = TutorialsConnectorHelper.connect(configurationBuilder);
      ProtostreamSchemaUploader schemaUploader = new ProtostreamSchemaUploader(cacheManager);
      System.out.println("Register proto schema in the server...");
      schemaUploader.registerSchema();

      System.out.println("DB content authors ...");
      System.out.println(dbCreator.readAuthors());
      System.out.println(String.format("Cache %s ...", AUTHORS_CACHE));
      System.out.println(cacheManager.getCache(AUTHORS_CACHE).values());
      System.out.println(String.format("Cache %s ...", BOOKS_CACHE));
      System.out.println(cacheManager.getCache(BOOKS_CACHE).values());

      System.out.println("Enter q to stop");
      Scanner scanner = new Scanner(System.in);
      String next = "";
      while (!next.equals("q")) {
         next = scanner.next();
      }

      cacheManager.administration().removeCache(AUTHORS_CACHE);
      cacheManager.administration().removeCache(BOOKS_CACHE);
      schemaUploader.unregisterSchema();
      TutorialsConnectorHelper.stop(cacheManager);
      dbCreator.stopDBServer();
   }
}
