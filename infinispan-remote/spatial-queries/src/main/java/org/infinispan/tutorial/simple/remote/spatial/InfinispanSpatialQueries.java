package org.infinispan.tutorial.simple.remote.spatial;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

import java.net.URI;
import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.api.query.QueryResult;
import org.infinispan.commons.api.query.geo.LatLng;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

/**
 * Copied and adapted from Katia's org.infinispan.tutorial.simple.remote.query.InfinispanRemoteQuery to
 * display spatial queries feature.
 */
public class InfinispanSpatialQueries {

   public static final String MY_CACHE_NAME = "my-cache";

   static RemoteCacheManager client;
   static RemoteCache<String, Object> myCache;

   private static final LatLng MILAN_COORDINATES = LatLng.of(45.4685, 9.1824);
   private static final LatLng COMO_COORDINATES = LatLng.of(45.8064, 9.0852);
   private static final LatLng BOLOGNA_COORDINATES = LatLng.of(44.4949, 11.3426);
   private static final LatLng MY_COORDINATES = LatLng.of(41.90847031512531, 12.455633288333539);
   private static final LatLng ROME_COORDINATES = LatLng.of(41.8967, 12.4822);
   private static final LatLng VENICE_COORDINATES = LatLng.of(45.4404, 12.3160);
   private static final LatLng SELVA_COORDINATES = LatLng.of(46.5560, 11.7559);

   public static void main(String[] args) throws Exception {
      connectToInfinispan();

      addDataToCache();
      withinCircle();
      withinBox();
      withinPolygon();
      spatialProjection();
      spatialOrderBy();
      spatialProjection_OrderBy();
      alternativeMapping_entityHavingMultipleGeoPoints();
      alternativeMapping_entityHavingMultipleGeoFields();

      disconnect(false);
   }

   static QueryResult<Restaurant> withinCircle() {
      Query<Restaurant> query = myCache.query("from tutorial.Restaurant r where r.location " +
            "within circle(:lat, :lon, :distance)");
      query.setParameter("lat", MY_COORDINATES.latitude());
      query.setParameter("lon", MY_COORDINATES.longitude());
      query.setParameter("distance", 100);
      QueryResult<Restaurant> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static QueryResult<Restaurant> withinBox() {
      Query<Restaurant> query = myCache.query("from tutorial.Restaurant r where r.location " +
            "within box(41.91, 12.45, 41.90, 12.46)");
      QueryResult<Restaurant> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static QueryResult<Restaurant> withinPolygon() {
      Query<Restaurant> query = myCache.query("from tutorial.Restaurant r where r.location " +
            "within polygon((41.91, 12.45), (41.91, 12.46), (41.90, 12.46), (41.90, 12.46))");
      QueryResult<Restaurant> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static List<RestaurantDTO> spatialProjection() {
      Query<Object[]> query = myCache.query("select r.name, distance(r.location, 41.90847031512531, 12.455633288333539) " +
            "from tutorial.Restaurant r");
      QueryResult<Object[]> queryResult = query.execute();
      List<RestaurantDTO> valueObjects = queryResult.list().stream()
            .map(r -> new RestaurantDTO((String) r[0], (Double) r[1]))
            .toList();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(valueObjects);
      return valueObjects;
   }

   static QueryResult<Restaurant> spatialOrderBy() {
      Query<Restaurant> query = myCache.query("from tutorial.Restaurant r where r.location " +
            "order by distance(r.location, 41.90847031512531, 12.455633288333539)");
      QueryResult<Restaurant> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static List<RestaurantDTO> spatialProjection_OrderBy() {
      Query<Object[]> query = myCache.query("select r.name, distance(r.location, 41.90847031512531, 12.455633288333539) " +
            "from tutorial.Restaurant r where r.location " +
            "order by distance(r.location, 41.90847031512531, 12.455633288333539)");
      QueryResult<Object[]> queryResult = query.execute();
      List<RestaurantDTO> valueObjects = queryResult.list().stream()
            .map(r -> new RestaurantDTO((String) r[0], (Double) r[1]))
            .toList();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(valueObjects);
      return valueObjects;
   }

   static QueryResult<TrainRoute> alternativeMapping_entityHavingMultipleGeoPoints() {
      Query<TrainRoute> query = myCache.query("from tutorial.TrainRoute r where r.departure " +
            "within circle(:lat, :lon, :distance)");
      query.setParameter("lat", BOLOGNA_COORDINATES.latitude());
      query.setParameter("lon", BOLOGNA_COORDINATES.longitude());
      query.setParameter("distance", 300_000);
      QueryResult<TrainRoute> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count().value());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static QueryResult<Hiking> alternativeMapping_entityHavingMultipleGeoFields() {
      Query<Hiking> query = myCache.query("from tutorial.Hiking h where h.start " +
            "within circle(:lat, :lon, :distance)");
      query.setParameter("lat", MY_COORDINATES.latitude());
      query.setParameter("lon", MY_COORDINATES.longitude());
      query.setParameter("distance", 150);
      QueryResult<Hiking> queryResult = query.execute();
      // Print the results
      System.out.println("COUNT " + queryResult.count());
      System.out.println(queryResult.list());
      return queryResult;
   }

   static void connectToInfinispan() throws Exception {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      // Add the Protobuf serialization context in the client
      builder.addContextInitializer(new TutorialSchemaImpl());

      // Use indexed cache
      URI indexedCacheURI = InfinispanSpatialQueries.class.getClassLoader().getResource("indexedCache.xml").toURI();
      builder.remoteCache(MY_CACHE_NAME).configurationURI(indexedCacheURI);

      // Connect to the server
      client = TutorialsConnectorHelper.connect(builder);

      // Create and add the Protobuf schema in the server
      addProtoSchema(client);

      // Get the people cache, create it if needed with the default configuration
      myCache = client.getCache(MY_CACHE_NAME);
   }

   static int addDataToCache() {
      myCache.put("La Locanda di Pietro", new Restaurant("La Locanda di Pietro",
            "Roman-style pasta dishes & Lazio region wines at a cozy traditional trattoria with a shaded terrace.",
            "Via Sebastiano Veniero, 28/c, 00192 Roma RM", 41.907903484609356, 12.45540543756422, 4.6f));
      myCache.put("Scialla The Original Street Food", new Restaurant("Scialla The Original Street Food",
            "Pastas & traditional pizza pies served in an unassuming eatery with vegetarian options.",
            "Vicolo del Farinone, 27, 00193 Roma RM", 41.90369455835456, 12.459566517195528, 4.7f));
      myCache.put("Trattoria Pizzeria Gli Archi", new Restaurant("Trattoria Pizzeria Gli Archi",
            "Traditional trattoria with exposed brick walls, serving up antipasti, pizzas & pasta dishes.",
            "Via Sebastiano Veniero, 26, 00192 Roma RM", 41.907930453801285, 12.455204785977637, 4.0f));
      myCache.put("Alla Bracioleria Gracchi Restaurant", new Restaurant("Alla Bracioleria Gracchi Restaurant",
            "", "Via dei Gracchi, 19, 00192 Roma RM", 41.907129402661795, 12.458927251586584, 4.7f ));
      myCache.put("Magazzino Scipioni", new Restaurant("Magazzino Scipioni",
            "Contemporary venue with a focus on unique wines & seasonal Italian plates, plus a bottle shop.",
            "Via degli Scipioni, 30, 00192 Roma RM", 41.90817843995448, 12.457118458698043, 4.6f));
      myCache.put("Dal Toscano Restaurant", new Restaurant("Dal Toscano Restaurant",
            "Rich pastas, signature steaks & classic Tuscan dishes, plus Chianti wines, at a venerable trattoria.",
            "Via Germanico, 58-60, 00192 Roma RM", 41.90785274056548, 12.45822050287784, 4.2f));
      myCache.put("Il Ciociaro", new Restaurant("Il Ciociaro",
            "Long-running, old-school restaurant plating traditional staples, from carbonara to tiramisu.",
            "Via Barletta, 21, 00192 Roma RM", 41.91038657525997, 12.458851939120656, 4.2f));

      myCache.put("Rome-Milan", new TrainRoute("Rome-Milan", ROME_COORDINATES.latitude(), ROME_COORDINATES.longitude(),
            MILAN_COORDINATES.latitude(), MILAN_COORDINATES.longitude()));
      myCache.put("Bologna-Selva", new TrainRoute("Bologna-Selva", BOLOGNA_COORDINATES.latitude(), BOLOGNA_COORDINATES.longitude(),
            SELVA_COORDINATES.latitude(), SELVA_COORDINATES.longitude()));
      myCache.put("Milan-Como", new TrainRoute("Milan-Como", MILAN_COORDINATES.latitude(), MILAN_COORDINATES.longitude(),
            COMO_COORDINATES.latitude(), COMO_COORDINATES.longitude()));
      myCache.put("Bologna-Venice", new TrainRoute("Bologna-Venice", BOLOGNA_COORDINATES.latitude(), BOLOGNA_COORDINATES.longitude(),
            VENICE_COORDINATES.latitude(), VENICE_COORDINATES.longitude()));

      myCache.put("1", new Hiking("track 1", LatLng.of(41.907903484609356, 12.45540543756422),
            LatLng.of(41.90369455835456, 12.459566517195528)));
      myCache.put("2", new Hiking("track 2", LatLng.of(41.90369455835456, 12.459566517195528),
            LatLng.of(41.907930453801285, 12.455204785977637)));
      myCache.put("3", new Hiking("track 3", LatLng.of(41.907930453801285, 12.455204785977637),
            LatLng.of(41.907903484609356, 12.45540543756422)));

      return myCache.size();
   }

   public static void disconnect(boolean removeCaches) {
      if (removeCaches) {
         client.administration().removeCache(MY_CACHE_NAME);
      }

      TutorialsConnectorHelper.stop(client);
   }

   private static void addProtoSchema(RemoteCacheManager cacheManager) {
      // Retrieve metadata cache
      RemoteCache<String, String> metadataCache =
            cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

      // Define the new schema on the server too
      GeneratedSchema schema = new TutorialSchemaImpl();
      metadataCache.put(schema.getProtoFileName(), schema.getProtoFile());
   }

   record RestaurantDTO(String name, Double distance){}
}
