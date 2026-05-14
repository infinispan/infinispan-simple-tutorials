'use strict';

const ispn = require('infinispan');
const protobuf = require('protobufjs');
const { setup, host, port, password } = require('../setup');

const restaurantProto = `syntax = "proto3";
package tutorial;
/**
 * @TypeId(1000060)
 * @Indexed
 * @GeoPoint(fieldName = "location", projectable = true, sortable = true)
 */
message Restaurant {
    /**
     * @Keyword(normalizer = "lowercase", projectable = true, sortable = true)
     */
    string name = 1;
    /**
     * @Text
     */
    string description = 2;
    /**
     * @Text
     */
    string address = 3;
    /**
     * @Latitude(fieldName = "location")
     */
    double latitude = 4;
    /**
     * @Longitude(fieldName = "location")
     */
    double longitude = 5;
    /**
     * @Basic
     */
    float score = 6;
}`;

const trainRouteProto = `syntax = "proto3";
package tutorial;
/**
 * @TypeId(1000061)
 * @Indexed
 * @GeoPoint(fieldName = "departure", projectable = true, sortable = true)
 * @GeoPoint(fieldName = "arrival", projectable = true, sortable = true)
 */
message TrainRoute {
    /**
     * @Keyword(normalizer = "lowercase")
     */
    string name = 1;
    /**
     * @Latitude(fieldName = "departure")
     */
    double departureLat = 2;
    /**
     * @Longitude(fieldName = "departure")
     */
    double departureLon = 3;
    /**
     * @Latitude(fieldName = "arrival")
     */
    double arrivalLat = 4;
    /**
     * @Longitude(fieldName = "arrival")
     */
    double arrivalLon = 5;
}`;

const MY_COORDINATES = {lat: 41.90847031512531, lon: 12.455633288333539};

const restaurants = [
  {name: 'La Locanda di Pietro', description: 'Roman-style pasta dishes & Lazio region wines at a cozy traditional trattoria with a shaded terrace.', address: 'Via Sebastiano Veniero, 28/c, 00192 Roma RM', latitude: 41.907903484609356, longitude: 12.45540543756422, score: 4.6},
  {name: 'Scialla The Original Street Food', description: 'Pastas & traditional pizza pies served in an unassuming eatery with vegetarian options.', address: 'Vicolo del Farinone, 27, 00193 Roma RM', latitude: 41.90369455835456, longitude: 12.459566517195528, score: 4.7},
  {name: 'Trattoria Pizzeria Gli Archi', description: 'Traditional trattoria with exposed brick walls, serving up antipasti, pizzas & pasta dishes.', address: 'Via Sebastiano Veniero, 26, 00192 Roma RM', latitude: 41.907930453801285, longitude: 12.455204785977637, score: 4.0},
  {name: 'Alla Bracioleria Gracchi Restaurant', description: '', address: 'Via dei Gracchi, 19, 00192 Roma RM', latitude: 41.907129402661795, longitude: 12.458927251586584, score: 4.7},
  {name: 'Magazzino Scipioni', description: 'Contemporary venue with a focus on unique wines & seasonal Italian plates, plus a bottle shop.', address: 'Via degli Scipioni, 30, 00192 Roma RM', latitude: 41.90817843995448, longitude: 12.457118458698043, score: 4.6},
  {name: 'Dal Toscano Restaurant', description: 'Rich pastas, signature steaks & classic Tuscan dishes, plus Chianti wines, at a venerable trattoria.', address: 'Via Germanico, 58-60, 00192 Roma RM', latitude: 41.90785274056548, longitude: 12.45822050287784, score: 4.2},
  {name: 'Il Ciociaro', description: 'Long-running, old-school restaurant plating traditional staples, from carbonara to tiramisu.', address: 'Via Barletta, 21, 00192 Roma RM', latitude: 41.91038657525997, longitude: 12.458851939120656, score: 4.2}
];

const ROME = {lat: 41.8967, lon: 12.4822};
const MILAN = {lat: 45.4685, lon: 9.1824};
const BOLOGNA = {lat: 44.4949, lon: 11.3426};
const COMO = {lat: 45.8064, lon: 9.0852};
const VENICE = {lat: 45.4404, lon: 12.3160};
const SELVA = {lat: 46.5560, lon: 11.7559};

const trainRoutes = [
  {name: 'Rome-Milan', departureLat: ROME.lat, departureLon: ROME.lon, arrivalLat: MILAN.lat, arrivalLon: MILAN.lon},
  {name: 'Bologna-Selva', departureLat: BOLOGNA.lat, departureLon: BOLOGNA.lon, arrivalLat: SELVA.lat, arrivalLon: SELVA.lon},
  {name: 'Milan-Como', departureLat: MILAN.lat, departureLon: MILAN.lon, arrivalLat: COMO.lat, arrivalLon: COMO.lon},
  {name: 'Bologna-Venice', departureLat: BOLOGNA.lat, departureLon: BOLOGNA.lon, arrivalLat: VENICE.lat, arrivalLon: VENICE.lon}
];

const authOpts = {
  authentication: {
    enabled: true,
    saslMechanism: 'SCRAM-SHA-256',
    userName: 'admin',
    password
  }
};

async function main() {
  await setup();

  const root = new protobuf.Root();
  protobuf.parse(restaurantProto, root);
  protobuf.parse(trainRouteProto, root);
  const Restaurant = root.lookupType('.tutorial.Restaurant');
  const TrainRoute = root.lookupType('.tutorial.TrainRoute');

  // Register schemas on the server
  const metaClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });

  try {
    await metaClient.put('tutorial/Restaurant.proto', restaurantProto);
    await metaClient.put('tutorial/TrainRoute.proto', trainRouteProto);
    console.log('Registered Restaurant and TrainRoute schemas.');
  } finally {
    await metaClient.disconnect();
  }

  // Create the indexed cache (schemas must be registered first)
  const adminClient = await ispn.client({port, host}, {
    ...authOpts,
    clientIntelligence: 'BASIC'
  });
  try {
    const cacheConfig = '<distributed-cache>' +
      '<encoding><key media-type="text/plain"/><value media-type="application/x-protostream"/></encoding>' +
      '<indexing enabled="true" storage="filesystem" startup-mode="AUTO">' +
      '<indexed-entities>' +
      '<indexed-entity>tutorial.Restaurant</indexed-entity>' +
      '<indexed-entity>tutorial.TrainRoute</indexed-entity>' +
      '</indexed-entities></indexing></distributed-cache>';
    await adminClient.admin.getOrCreateCache('spatialCache', cacheConfig);
  } finally {
    await adminClient.disconnect();
  }

  // Connect to the indexed spatial cache
  const client = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'spatialCache',
    topologyUpdates: false,
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'application/x-protostream'
    }
  });

  try {
    client.registerProtostreamRoot(root);
    client.registerProtostreamType('.tutorial.Restaurant', 1000060);
    client.registerProtostreamType('.tutorial.TrainRoute', 1000061);

    await client.clear();

    // Add restaurants
    for (const r of restaurants) {
      const msg = Restaurant.create(r);
      await client.put(r.name, msg);
    }
    console.log(`Added ${restaurants.length} restaurants in Rome.\n`);

    // Add train routes
    for (const r of trainRoutes) {
      const msg = TrainRoute.create(r);
      await client.put(r.name, msg);
    }
    console.log(`Added ${trainRoutes.length} train routes.\n`);

    // Within circle: restaurants within 100m of my coordinates
    console.log('=== Within circle (100m radius) ===');
    const circle = await client.query({
      queryString: `from tutorial.Restaurant r where r.location within circle(${MY_COORDINATES.lat}, ${MY_COORDINATES.lon}, 100)`
    });
    console.log(`Found ${circle.length} restaurants:`);
    circle.forEach(r => console.log(`  ${r.name}`));

    // Within box: restaurants in a bounding box
    console.log('\n=== Within box ===');
    const box = await client.query({
      queryString: 'from tutorial.Restaurant r where r.location within box(41.91, 12.45, 41.90, 12.46)'
    });
    console.log(`Found ${box.length} restaurants:`);
    box.forEach(r => console.log(`  ${r.name}`));

    // Within polygon: restaurants in a polygon area
    console.log('\n=== Within polygon ===');
    const polygon = await client.query({
      queryString: 'from tutorial.Restaurant r where r.location within polygon((41.91, 12.45), (41.91, 12.46), (41.90, 12.46), (41.90, 12.46))'
    });
    console.log(`Found ${polygon.length} restaurants:`);
    polygon.forEach(r => console.log(`  ${r.name}`));

    // Distance projection: show distance from my coordinates
    console.log('\n=== Distance projection ===');
    const distances = await client.query({
      queryString: `select r.name, distance(r.location, ${MY_COORDINATES.lat}, ${MY_COORDINATES.lon}) from tutorial.Restaurant r`
    });
    distances.forEach(row => console.log(`  ${row[0]}: ${Math.round(row[1])}m`));

    // Order by distance
    console.log('\n=== Order by distance ===');
    const ordered = await client.query({
      queryString: `from tutorial.Restaurant r where r.location order by distance(r.location, ${MY_COORDINATES.lat}, ${MY_COORDINATES.lon})`
    });
    ordered.forEach(r => console.log(`  ${r.name}`));

    // Train routes: departures within 300km of Bologna
    console.log('\n=== Train routes departing near Bologna (300km) ===');
    const routes = await client.query({
      queryString: `from tutorial.TrainRoute r where r.departure within circle(${BOLOGNA.lat}, ${BOLOGNA.lon}, 300000)`
    });
    console.log(`Found ${routes.length} routes:`);
    routes.forEach(r => console.log(`  ${r.name}`));

    await client.clear();
    console.log('\nCache cleared.');
  } finally {
    await client.disconnect();
  }

  // Clean up schemas
  const cleanupClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });
  try {
    await cleanupClient.remove('tutorial/Restaurant.proto');
    await cleanupClient.remove('tutorial/TrainRoute.proto');
  } finally {
    await cleanupClient.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
