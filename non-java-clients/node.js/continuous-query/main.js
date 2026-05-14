'use strict';

const ispn = require('infinispan');
const protobuf = require('protobufjs');
const { setup, host, port, password } = require('../setup');

const instaPostProto = `syntax = "proto3";
package tutorial;
/**
 * @TypeId(1000050)
 */
message InstaPost {
    string id = 1;
    string user = 2;
    string hashtag = 3;
}`;

const authOpts = {
  authentication: {
    enabled: true,
    saslMechanism: 'SCRAM-SHA-256',
    userName: 'admin',
    password
  }
};

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function main() {
  await setup();

  const root = protobuf.parse(instaPostProto).root;
  const InstaPost = root.lookupType('.tutorial.InstaPost');

  // Register the schema on the server
  const metaClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });

  try {
    await metaClient.put('tutorial/InstaPost.proto', instaPostProto);
    console.log('Registered InstaPost.proto schema.');
  } finally {
    await metaClient.disconnect();
  }

  // Connect to a protostream cache
  const client = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'protoStreamCache',
    topologyUpdates: false,
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'application/x-protostream'
    }
  });

  try {
    client.registerProtostreamRoot(root);
    client.registerProtostreamType('.tutorial.InstaPost', 1000050);

    await client.clear();

    // Register a continuous query filtering posts by user
    const cq = await client.addContinuousQuery(
      'FROM tutorial.InstaPost p WHERE p.user = :userName',
      {params: {userName: 'belen_esteban'}}
    );

    let matchCount = 0;
    cq.on('joining', function(key, value) {
      matchCount++;
      console.log(`[JOINING] @belen_esteban posted! (total: ${matchCount})`);
    });

    cq.on('leaving', function(key) {
      console.log('[LEAVING] Post no longer matches.');
    });

    console.log('Continuous query registered.\n');

    // Add random posts
    const users = ['alice', 'bob', 'belen_esteban', 'charlie'];
    const numPosts = 20;
    for (let i = 0; i < numPosts; i++) {
      const user = users[Math.floor(Math.random() * users.length)];
      const post = InstaPost.create({id: `post-${i}`, user: user, hashtag: 'infinispan'});
      await client.put(String(i), post);
    }

    // Wait for events to be delivered
    await sleep(2000);

    console.log(`\nTotal posts: ${numPosts}`);
    console.log(`Posts by @belen_esteban: ${matchCount}`);

    // Remove the continuous query
    await client.removeContinuousQuery(cq);
    console.log('Continuous query removed.');

    await client.clear();
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
    await cleanupClient.remove('tutorial/InstaPost.proto');
  } finally {
    await cleanupClient.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
