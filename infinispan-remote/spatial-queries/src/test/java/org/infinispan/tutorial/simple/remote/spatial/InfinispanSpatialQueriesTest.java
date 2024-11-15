package org.infinispan.tutorial.simple.remote.spatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.addDataToCache;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.alternativeMapping_entityHavingMultipleGeoFields;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.alternativeMapping_entityHavingMultipleGeoPoints;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.spatialOrderBy;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.spatialProjection;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.spatialProjection_OrderBy;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.withinBox;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.withinCircle;
import static org.infinispan.tutorial.simple.remote.spatial.InfinispanSpatialQueries.withinPolygon;

import java.util.List;

import org.infinispan.commons.api.query.QueryResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InfinispanSpatialQueriesTest {

   @BeforeAll
   public static void start() throws Exception {
      InfinispanSpatialQueries.connectToInfinispan();
   }

   @AfterAll
   public static void stop() {
      InfinispanSpatialQueries.disconnect(true);
   }

   @Test
   public void testSpatialQueries() {
      int size = addDataToCache();
      assertThat(size).isEqualTo(14);

      QueryResult<Restaurant> queryResult = withinCircle();
      assertThat(queryResult.count().value()).isEqualTo(2);
      assertThat(queryResult.list()).extracting(Restaurant::name)
            .containsExactlyInAnyOrder("La Locanda di Pietro", "Trattoria Pizzeria Gli Archi");

      queryResult = withinBox();
      assertThat(queryResult.count().value()).isEqualTo(6);
      assertThat(queryResult.list()).extracting(Restaurant::name)
            .containsExactlyInAnyOrder("La Locanda di Pietro", "Trattoria Pizzeria Gli Archi", "Magazzino Scipioni",
                  "Dal Toscano Restaurant", "Scialla The Original Street Food", "Alla Bracioleria Gracchi Restaurant");

      queryResult = withinPolygon();
      assertThat(queryResult.count().value()).isEqualTo(6);
      assertThat(queryResult.list()).extracting(Restaurant::name)
            .containsExactlyInAnyOrder("La Locanda di Pietro", "Trattoria Pizzeria Gli Archi", "Magazzino Scipioni",
                  "Dal Toscano Restaurant", "Scialla The Original Street Food", "Alla Bracioleria Gracchi Restaurant");

      List<InfinispanSpatialQueries.RestaurantDTO> projection = spatialProjection();
      assertThat(projection)
            .filteredOn(r -> r.distance().equals(65.78997502576355)).extracting(InfinispanSpatialQueries.RestaurantDTO::name)
            .first().isEqualTo("La Locanda di Pietro");
      assertThat(projection)
            .filteredOn(r -> r.distance().equals(622.8579549605669)).extracting(InfinispanSpatialQueries.RestaurantDTO::name)
            .first().isEqualTo("Scialla The Original Street Food");

      queryResult = spatialOrderBy();
      assertThat(queryResult.count().value()).isEqualTo(7);
      assertThat(queryResult.list()).extracting(Restaurant::name)
      .containsExactly("La Locanda di Pietro", "Trattoria Pizzeria Gli Archi", "Magazzino Scipioni",
            "Dal Toscano Restaurant", "Alla Bracioleria Gracchi Restaurant", "Il Ciociaro",
            "Scialla The Original Street Food");

      projection = spatialProjection_OrderBy();
      assertThat(projection).extracting(InfinispanSpatialQueries.RestaurantDTO::name)
            .containsExactly("La Locanda di Pietro", "Trattoria Pizzeria Gli Archi", "Magazzino Scipioni",
                  "Dal Toscano Restaurant", "Alla Bracioleria Gracchi Restaurant", "Il Ciociaro",
                  "Scialla The Original Street Food");
      assertThat(projection).extracting(InfinispanSpatialQueries.RestaurantDTO::distance)
            .containsExactly(65.78997502576355, 69.72458363789359, 127.11531555461053, 224.8438726836208,
                  310.6984480274634, 341.0897945700656, 622.8579549605669);

      QueryResult<TrainRoute> trainQueryResult = alternativeMapping_entityHavingMultipleGeoPoints();
      assertThat(trainQueryResult.count().value()).isEqualTo(3);
      assertThat(trainQueryResult.list()).extracting(TrainRoute::name)
            .containsExactlyInAnyOrder("Milan-Como", "Bologna-Venice", "Bologna-Selva");

      QueryResult<Hiking> hikingQueryResult = alternativeMapping_entityHavingMultipleGeoFields();
      assertThat(hikingQueryResult.count().value()).isEqualTo(2);
      assertThat(hikingQueryResult.list()).extracting(Hiking::name)
            .containsExactlyInAnyOrder("track 1", "track 3");
   }
}
