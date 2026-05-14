'use strict';

const ispn = require('infinispan');
const protobuf = require('protobufjs');
const { setup, host, port, password } = require('../setup');

const beerProto = `syntax = "proto3";
package quickstart;
/**
 * @TypeId(1000050)
 * @Indexed
 */
message Beer {
    /** @Keyword(projectable = true, sortable = true) */
    string name = 1;
    /** @Keyword(projectable = true, normalizer = "lowercase") */
    string style = 2;
    /** @Keyword(projectable = true, sortable = true, normalizer = "lowercase") */
    string brewery = 3;
    /** @Keyword(projectable = true, normalizer = "lowercase") */
    string country = 4;
    /** @Basic(projectable = true, sortable = true) */
    double abv = 5;
    /** @Text */
    string description = 6;
    /** @Vector(dimension = 3, similarity = COSINE) */
    repeated float descriptionEmbedding = 7;
}`;

const CACHE_CONFIG =
  '<distributed-cache>' +
  '<encoding><key media-type="text/plain"/><value media-type="application/x-protostream"/></encoding>' +
  '<indexing enabled="true" storage="filesystem" startup-mode="AUTO">' +
  '<indexed-entities><indexed-entity>quickstart.Beer</indexed-entity></indexed-entities>' +
  '</indexing></distributed-cache>';

// Vectors: [dark/roasty, light/crisp, hoppy/craft]
const beers = [
  {name: 'Guinness', style: 'Stout', brewery: 'Guinness Brewery', country: 'Ireland', abv: 4.2,
   description: 'A rich, creamy stout with deep roasted barley flavours, hints of coffee and chocolate, and a velvety smooth finish.',
   descriptionEmbedding: [0.95, 0.05, 0.10]},
  {name: 'Delirium', style: 'Belgian Strong Ale', brewery: 'Brouwerij Huyghe', country: 'Belgium', abv: 8.5,
   description: 'A complex strong blonde ale with fruity esters, spicy phenols, and a warming alcohol presence balanced by a dry finish.',
   descriptionEmbedding: [0.30, 0.30, 0.70]},
  {name: 'Estrella Galicia', style: 'Lager', brewery: 'Hijos de Rivera', country: 'Spain', abv: 5.5,
   description: 'A crisp European lager with a balanced malt backbone, mild hop bitterness, and a clean refreshing finish.',
   descriptionEmbedding: [0.10, 0.90, 0.15]},
  {name: 'Mahou', style: 'Pilsner', brewery: 'Mahou San Miguel', country: 'Spain', abv: 5.5,
   description: 'A golden pilsner with delicate floral hop aromas, light biscuity malt, and a bright effervescent character.',
   descriptionEmbedding: [0.05, 0.85, 0.25]},
  {name: 'Corona Extra', style: 'Pale Lager', brewery: 'Grupo Modelo', country: 'Mexico', abv: 4.5,
   description: 'A light, easy-drinking pale lager with subtle sweetness, a hint of citrus, and a crisp dry finish best enjoyed ice-cold.',
   descriptionEmbedding: [0.05, 0.95, 0.10]},
  {name: 'Tactical Nuclear Penguin', style: 'Imperial Stout', brewery: 'BrewDog', country: 'Scotland', abv: 32.0,
   description: 'An extreme imperial stout aged in whisky casks, intensely smoky with dark chocolate, coffee, and dried fruit notes.',
   descriptionEmbedding: [0.98, 0.02, 0.20]},
  {name: 'Brahma', style: 'Lager', brewery: 'Ambev', country: 'Brazil', abv: 4.3,
   description: 'A light Brazilian lager, smooth and mildly sweet, brewed for easy drinking in warm weather.',
   descriptionEmbedding: [0.05, 0.92, 0.05]},
  {name: 'Radegast', style: 'Czech Lager', brewery: 'Radegast Brewery', country: 'Czech Republic', abv: 5.0,
   description: 'A traditional Czech lager with a prominent Saaz hop aroma, bready malt character, and a crisp bitter finish.',
   descriptionEmbedding: [0.15, 0.80, 0.30]},
  {name: 'Turia', style: 'Märzen', brewery: 'Turia Brewery', country: 'Spain', abv: 5.4,
   description: 'A toasted amber märzen from Valencia with caramel malt sweetness, a nutty aroma, and a smooth medium body.',
   descriptionEmbedding: [0.60, 0.40, 0.15]},
  {name: 'Hoptimus Prime', style: 'IPA', brewery: 'Hoptimus Brewing', country: 'USA', abv: 7.5,
   description: 'An aggressively hopped American IPA bursting with tropical fruit, pine resin, and grapefruit citrus over a sturdy malt backbone.',
   descriptionEmbedding: [0.10, 0.15, 0.95]},
  {name: 'Pagoa', style: 'Basque Ale', brewery: 'Pagoa Brewery', country: 'Spain', abv: 5.0,
   description: 'A craft ale from the Basque Country with earthy hops, a light fruity character, and a balanced malty sweetness.',
   descriptionEmbedding: [0.25, 0.35, 0.60]}
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
  protobuf.parse(beerProto, root);
  const Beer = root.lookupType('.quickstart.Beer');

  // Register schema on the server
  const metaClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });
  try {
    await metaClient.put('quickstart/Beer.proto', beerProto);
    console.log('Registered Beer schema.');
  } finally {
    await metaClient.disconnect();
  }

  // Create the indexed cache
  const adminClient = await ispn.client({port, host}, {
    ...authOpts,
    clientIntelligence: 'BASIC'
  });
  try {
    await adminClient.admin.getOrCreateCache('beers', CACHE_CONFIG);
  } finally {
    await adminClient.disconnect();
  }

  // Connect to the cache with protostream encoding
  const client = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'beers',
    topologyUpdates: false,
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'application/x-protostream'
    }
  });

  try {
    client.registerProtostreamRoot(root);
    client.registerProtostreamType('.quickstart.Beer', 1000050);

    await client.clear();

    // Populate beers
    for (let i = 0; i < beers.length; i++) {
      const msg = Beer.create(beers[i]);
      await client.put(`beer:${i + 1}`, msg);
    }
    console.log(`Loaded ${beers.length} beers.\n`);

    // --- Traditional Ickle queries ---

    // Full-text search
    console.log("=== Full-text search: beers mentioning 'chocolate' ===");
    const chocolate = await client.query({
      queryString: "from quickstart.Beer b where b.description : 'chocolate'"
    });
    chocolate.forEach(b => console.log(`  ${pad(b.name, 30)} ${b.style}`));
    console.log();

    // Keyword + range filter
    console.log('=== Keyword + range: Spanish beers under 5.5% ABV ===');
    const spanish = await client.query({
      queryString: "from quickstart.Beer b where b.country = 'Spain' and b.abv < 5.5"
    });
    spanish.forEach(b => console.log(`  ${pad(b.name, 30)} ${b.abv.toFixed(1)}%`));
    console.log();

    // Projections sorted by ABV
    console.log('=== Projections: all beers sorted by ABV ===');
    const sorted = await client.query({
      queryString: 'select b.name, b.style, b.abv from quickstart.Beer b order by b.abv'
    });
    sorted.forEach(row => console.log(`  ${pad(row[0], 30)} ${pad(row[1], 20)} ${row[2].toFixed(1)}%`));
    console.log();

    // --- Vector search (kNN) ---

    // kNN: 3 closest to "dark roasty" vector
    console.log("=== kNN: 3 beers closest to 'dark roasty' vector [0.9, 0.1, 0.1] ===");
    const knn = await client.query({
      queryString: 'from quickstart.Beer b where b.descriptionEmbedding <-> [0.9, 0.1, 0.1]~3'
    });
    knn.forEach(b => console.log(`  ${pad(b.name, 30)} ${b.style}`));
    console.log();

    // kNN with score projection
    console.log("=== kNN with score: 3 beers closest to 'light crisp lager' vector [0.05, 0.9, 0.1] ===");
    const scored = await client.query({
      queryString: 'select b.name, b.style, score(b) from quickstart.Beer b where b.descriptionEmbedding <-> [0.05, 0.9, 0.1]~3'
    });
    scored.forEach(row => console.log(`  ${pad(row[0], 30)} ${pad(row[1], 20)} score=${row[2].toFixed(4)}`));
    console.log();

    // --- Hybrid queries: vector + metadata ---

    // Hybrid: refreshing summer beer, only lagers under 5% ABV
    console.log("=== Hybrid: closest to 'refreshing summer beer' [0.05, 0.95, 0.05], only lagers under 5% ABV ===");
    const hybrid1 = await client.query({
      queryString: "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
        "where b.descriptionEmbedding <-> [0.05, 0.95, 0.05]~3 " +
        "filtering (b.style = 'Lager' and b.abv < 5.0)"
    });
    if (hybrid1.length === 0) {
      console.log('  (no matches)');
    }
    hybrid1.forEach(row => console.log(`  score=${row[0].toFixed(4)}  ${pad(row[1], 30)} ${pad(row[2], 10)} ${row[3].toFixed(1)}%`));
    console.log();

    // Hybrid: toasted malty caramel, only Spanish beers
    console.log("=== Hybrid: closest to 'toasted malty caramel' [0.7, 0.3, 0.1], only Spanish beers ===");
    const hybrid2 = await client.query({
      queryString: "select score(b), b.name, b.style, b.abv from quickstart.Beer b " +
        "where b.descriptionEmbedding <-> [0.7, 0.3, 0.1]~3 filtering b.country = 'Spain'"
    });
    hybrid2.forEach(row => console.log(`  score=${row[0].toFixed(4)}  ${pad(row[1], 30)} ${pad(row[2], 15)} ${row[3].toFixed(1)}%`));
    console.log();

    // Hybrid: hoppy craft + description mentions "citrus"
    console.log("=== Hybrid: closest to 'hoppy craft' [0.1, 0.1, 0.95], description mentions 'citrus' ===");
    const hybrid3 = await client.query({
      queryString: "select score(b), b.name, b.brewery, b.abv from quickstart.Beer b " +
        "where b.descriptionEmbedding <-> [0.1, 0.1, 0.95]~5 " +
        "filtering b.description : 'citrus'"
    });
    hybrid3.forEach(row => console.log(`  score=${row[0].toFixed(4)}  ${pad(row[1], 30)} ${pad(row[2], 20)} ${row[3].toFixed(1)}%`));
    console.log();

    await client.clear();
    console.log('Cache cleared.');
  } finally {
    await client.disconnect();
  }

  // Clean up schema
  const cleanupClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });
  try {
    await cleanupClient.remove('quickstart/Beer.proto');
  } finally {
    await cleanupClient.disconnect();
  }
}

function pad(str, len) {
  return (str || '').toString().padEnd(len);
}

main().catch(err => { console.error(err); process.exit(1); });
