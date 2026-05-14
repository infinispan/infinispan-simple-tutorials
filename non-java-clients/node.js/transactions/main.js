'use strict';

const ispn = require('infinispan');
const { setup, host, port, password } = require('../setup');

const authOpts = {
  authentication: {
    enabled: true,
    saslMechanism: 'SCRAM-SHA-256',
    userName: 'admin',
    password
  }
};

const TX_CACHE_CONFIG =
  '<distributed-cache>' +
  '<encoding><key media-type="text/plain"/><value media-type="text/plain"/></encoding>' +
  '<transaction mode="NON_XA" locking="PESSIMISTIC"/>' +
  '</distributed-cache>';

async function main() {
  await setup();

  // Create a transactional cache
  const adminClient = await ispn.client({port, host}, {
    ...authOpts,
    clientIntelligence: 'BASIC'
  });
  try {
    await adminClient.admin.getOrCreateCache('txCache', TX_CACHE_CONFIG);
  } finally {
    await adminClient.disconnect();
  }

  // Connect to the transactional cache
  const client = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'txCache',
    topologyUpdates: false,
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'text/plain'
    }
  });

  try {
    await client.clear();

    // --- Commit: put three keys atomically ---
    console.log('=== Commit transaction ===');
    const tm = client.getTransactionManager();
    await tm.begin();

    await client.put('key1', 'value1');
    await client.put('key2', 'value2');
    await client.put('key3', 'value3');
    console.log('Put 3 keys (buffered locally).');

    // Read within the transaction returns the buffered value
    const v = await client.get('key1');
    console.log(`Get key1 within tx: ${v}`);

    await tm.commit();
    console.log('Transaction committed.');

    // Verify all values are on the server
    console.log(`  key1 = ${await client.get('key1')}`);
    console.log(`  key2 = ${await client.get('key2')}`);
    console.log(`  key3 = ${await client.get('key3')}`);

    // --- Rollback: changes are discarded ---
    console.log('\n=== Rollback transaction ===');
    await tm.begin();
    await client.put('key4', 'should-not-exist');
    console.log('Put key4 (buffered locally).');
    await tm.rollback();
    console.log('Transaction rolled back.');
    console.log(`  key4 = ${await client.get('key4')}`);

    // --- Read-then-write: version-based conflict detection ---
    console.log('\n=== Read-then-write transaction ===');
    await client.put('counter', '0');

    await tm.begin();
    const current = await client.get('counter');
    console.log(`Read counter within tx: ${current}`);
    await client.put('counter', String(parseInt(current) + 1));
    await tm.commit();
    console.log('Transaction committed.');
    console.log(`  counter = ${await client.get('counter')}`);

    // --- Remove within a transaction ---
    console.log('\n=== Remove within transaction ===');
    await client.put('temp', 'ephemeral');

    await tm.begin();
    await client.remove('temp');
    const removed = await client.get('temp');
    console.log(`Get temp within tx after remove: ${removed}`);
    await tm.commit();
    console.log('Transaction committed.');
    console.log(`  temp = ${await client.get('temp')}`);

    await client.clear();
    console.log('\nCache cleared.');
  } finally {
    await client.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
