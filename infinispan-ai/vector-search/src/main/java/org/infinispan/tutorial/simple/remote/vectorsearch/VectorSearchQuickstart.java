package org.infinispan.tutorial.simple.remote.vectorsearch;

import java.net.URI;
import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.query.Query;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * Vector search quickstart using a beer catalogue.
 * <p>
 * Every beer in this dataset is named after an Infinispan release codename.
 * <p>
 * The 3-dimensional vectors are hand-crafted so that beers of similar style
 * cluster together:
 * <ul>
 *   <li>Axis 0 — dark/roasty character</li>
 *   <li>Axis 1 — light/crisp/lager character</li>
 *   <li>Axis 2 — hoppy/craft character</li>
 * </ul>
 * In a real application you would use an embedding model (e.g. all-MiniLM-L6-v2)
 * and set the vector dimension accordingly (384 for that model).
 */
public class VectorSearchQuickstart {

   public static final String CACHE_NAME = "beers";

   static RemoteCacheManager cacheManager;
   static RemoteCache<String, Beer> cache;

   public static void main(String[] args) throws Exception {
      connect();
      populateBeers();
      System.out.println("Loaded " + cache.size() + " beers.\n");

      fullTextSearch();
      keywordAndRangeFilter();
      projectionsAndSorting();
      knnVectorSearch();
      scoreProjection();
      hybridFilterByStyle();
      hybridFilterByCountry();
      hybridFullTextAndVector();

      disconnect();
   }

   static void connect() throws Exception {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();
      builder.addContextInitializer(new BeerSchemaImpl());

      URI cacheConfigURI = VectorSearchQuickstart.class.getClassLoader()
            .getResource("indexedCache.xml").toURI();
      builder.remoteCache(CACHE_NAME).configurationURI(cacheConfigURI);

      cacheManager = TutorialsConnectorHelper.connect(builder);
      cacheManager.administration().schemas().createOrUpdate(new BeerSchemaImpl());
      cache = cacheManager.getCache(CACHE_NAME);
   }

   static void disconnect() {
      TutorialsConnectorHelper.stop(cacheManager);
   }

   static void populateBeers() {
      // Vectors: [dark/roasty, light/crisp, hoppy/craft]
      cache.put("beer:1", new Beer(
            "Guinness", "Stout", "Guinness Brewery", "Ireland", 4.2,
            "A rich, creamy stout with deep roasted barley flavours, hints of coffee and chocolate, and a velvety smooth finish.",
            new float[]{0.95f, 0.05f, 0.10f}
      ));
      cache.put("beer:2", new Beer(
            "Delirium", "Belgian Strong Ale", "Brouwerij Huyghe", "Belgium", 8.5,
            "A complex strong blonde ale with fruity esters, spicy phenols, and a warming alcohol presence balanced by a dry finish.",
            new float[]{0.30f, 0.30f, 0.70f}
      ));
      cache.put("beer:3", new Beer(
            "Estrella Galicia", "Lager", "Hijos de Rivera", "Spain", 5.5,
            "A crisp European lager with a balanced malt backbone, mild hop bitterness, and a clean refreshing finish.",
            new float[]{0.10f, 0.90f, 0.15f}
      ));
      cache.put("beer:4", new Beer(
            "Mahou", "Pilsner", "Mahou San Miguel", "Spain", 5.5,
            "A golden pilsner with delicate floral hop aromas, light biscuity malt, and a bright effervescent character.",
            new float[]{0.05f, 0.85f, 0.25f}
      ));
      cache.put("beer:5", new Beer(
            "Corona Extra", "Pale Lager", "Grupo Modelo", "Mexico", 4.5,
            "A light, easy-drinking pale lager with subtle sweetness, a hint of citrus, and a crisp dry finish best enjoyed ice-cold.",
            new float[]{0.05f, 0.95f, 0.10f}
      ));
      cache.put("beer:6", new Beer(
            "Tactical Nuclear Penguin", "Imperial Stout", "BrewDog", "Scotland", 32.0,
            "An extreme imperial stout aged in whisky casks, intensely smoky with dark chocolate, coffee, and dried fruit notes.",
            new float[]{0.98f, 0.02f, 0.20f}
      ));
      cache.put("beer:7", new Beer(
            "Brahma", "Lager", "Ambev", "Brazil", 4.3,
            "A light Brazilian lager, smooth and mildly sweet, brewed for easy drinking in warm weather.",
            new float[]{0.05f, 0.92f, 0.05f}
      ));
      cache.put("beer:8", new Beer(
            "Radegast", "Czech Lager", "Radegast Brewery", "Czech Republic", 5.0,
            "A traditional Czech lager with a prominent Saaz hop aroma, bready malt character, and a crisp bitter finish.",
            new float[]{0.15f, 0.80f, 0.30f}
      ));
      cache.put("beer:9", new Beer(
            "Turia", "Märzen", "Turia Brewery", "Spain", 5.4,
            "A toasted amber märzen from Valencia with caramel malt sweetness, a nutty aroma, and a smooth medium body.",
            new float[]{0.60f, 0.40f, 0.15f}
      ));
      cache.put("beer:10", new Beer(
            "Hoptimus Prime", "IPA", "Hoptimus Brewing", "USA", 7.5,
            "An aggressively hopped American IPA bursting with tropical fruit, pine resin, and grapefruit citrus over a sturdy malt backbone.",
            new float[]{0.10f, 0.15f, 0.95f}
      ));
      cache.put("beer:11", new Beer(
            "Pagoa", "Basque Ale", "Pagoa Brewery", "Spain", 5.0,
            "A craft ale from the Basque Country with earthy hops, a light fruity character, and a balanced malty sweetness.",
            new float[]{0.25f, 0.35f, 0.60f}
      ));
   }

   // ---- Traditional Ickle queries ----

   static void fullTextSearch() {
      System.out.println("=== Full-text search: beers mentioning 'chocolate' ===");
      Query<Beer> query = cache.query("from quickstart.Beer b where b.description : 'chocolate'");
      for (Beer beer : query.list()) {
         System.out.printf("  %-30s %s%n", beer.name(), beer.style());
      }
      System.out.println();
   }

   static void keywordAndRangeFilter() {
      System.out.println("=== Keyword + range: Spanish beers under 5.5% ABV ===");
      Query<Beer> query = cache.query(
            "from quickstart.Beer b where b.country = 'Spain' and b.abv < 5.5");
      for (Beer beer : query.list()) {
         System.out.printf("  %-30s %.1f%%%n", beer.name(), beer.abv());
      }
      System.out.println();
   }

   static void projectionsAndSorting() {
      System.out.println("=== Projections: all beers sorted by ABV ===");
      Query<Object[]> query = cache.query(
            "select b.name, b.style, b.abv from quickstart.Beer b order by b.abv");
      for (Object[] row : query.list()) {
         System.out.printf("  %-30s %-20s %.1f%%%n", row[0], row[1], row[2]);
      }
      System.out.println();
   }

   // ---- Vector search (kNN) ----

   static void knnVectorSearch() {
      System.out.println("=== kNN: 3 beers closest to 'dark roasty' vector [0.9, 0.1, 0.1] ===");
      Query<Beer> query = cache.query(
            "from quickstart.Beer b where b.descriptionEmbedding <-> [:v]~:k");
      query.setParameter("v", new float[]{0.9f, 0.1f, 0.1f});
      query.setParameter("k", 3);
      for (Beer beer : query.list()) {
         System.out.printf("  %-30s %s%n", beer.name(), beer.style());
      }
      System.out.println();
   }

   static void scoreProjection() {
      System.out.println("=== kNN with score: 3 beers closest to 'light crisp lager' vector [0.05, 0.9, 0.1] ===");
      Query<Object[]> query = cache.query(
            "select b.name, b.style, score(b) from quickstart.Beer b " +
            "where b.descriptionEmbedding <-> [:v]~:k");
      query.setParameter("v", new float[]{0.05f, 0.9f, 0.1f});
      query.setParameter("k", 3);
      for (Object[] row : query.list()) {
         System.out.printf("  %-30s %-20s score=%.4f%n", row[0], row[1], (Float) row[2]);
      }
      System.out.println();
   }

   // ---- Hybrid queries: vector + metadata ----

   static void hybridFilterByStyle() {
      System.out.println("=== Hybrid: closest to 'refreshing summer beer' [0.05, 0.95, 0.05], only lagers under 5% ABV ===");
      Query<Object[]> query = cache.query(
            "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
            "where b.descriptionEmbedding <-> [:v]~:k " +
            "filtering (b.style = 'Lager' and b.abv < 5.0)");
      query.setParameter("v", new float[]{0.05f, 0.95f, 0.05f});
      query.setParameter("k", 3);
      List<Object[]> results = query.list();
      if (results.isEmpty()) {
         System.out.println("  (no matches)");
      }
      for (Object[] row : results) {
         System.out.printf("  score=%.4f  %-30s %-10s %.1f%%%n", row[0], row[1], row[2], row[3]);
      }
      System.out.println();
   }

   static void hybridFilterByCountry() {
      System.out.println("=== Hybrid: closest to 'toasted malty caramel' [0.7, 0.3, 0.1], only Spanish beers ===");
      Query<Object[]> query = cache.query(
            "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
            "where b.descriptionEmbedding <-> [:v]~:k filtering b.country = 'Spain'");
      query.setParameter("v", new float[]{0.7f, 0.3f, 0.1f});
      query.setParameter("k", 3);
      for (Object[] row : query.list()) {
         System.out.printf("  score=%.4f  %-30s %-15s %.1f%%%n", row[0], row[1], row[2], row[3]);
      }
      System.out.println();
   }

   static void hybridFullTextAndVector() {
      System.out.println("=== Hybrid: closest to 'hoppy craft' [0.1, 0.1, 0.95], description mentions 'citrus' ===");
      Query<Object[]> query = cache.query(
            "select score(b), b.name, b.brewery, b.abv from quickstart.Beer b " +
            "where b.descriptionEmbedding <-> [:v]~:k " +
            "filtering b.description : 'citrus'");
      query.setParameter("v", new float[]{0.1f, 0.1f, 0.95f});
      query.setParameter("k", 5);
      for (Object[] row : query.list()) {
         System.out.printf("  score=%.4f  %-30s %-20s %.1f%%%n", row[0], row[1], row[2], row[3]);
      }
      System.out.println();
   }
}
