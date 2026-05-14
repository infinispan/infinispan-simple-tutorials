'use strict';

const ispn = require('infinispan');
const { setup, host, port, password } = require('../setup');

async function main() {
  await setup();

  const client = await ispn.client({port, host}, {
    cacheName: 'test',
    authentication: {
      enabled: true,
      saslMechanism: 'SCRAM-SHA-256',
      userName: 'admin',
      password
    }
  });

  try {
    // Put and get a single entry
    await client.put('name', 'Infinispan');
    const name = await client.get('name');
    console.log(`get(name) = ${name}`);

    // Check if a key exists
    const exists = await client.containsKey('name');
    console.log(`containsKey(name) = ${exists}`);

    // Put multiple entries at once
    const entries = [
      {key: 'key1', value: 'value1'},
      {key: 'key2', value: 'value2'},
      {key: 'key3', value: 'value3'}
    ];
    await client.putAll(entries);

    // Get multiple entries
    const keys = entries.map(e => e.key);
    const result = await client.getAll(keys);
    console.log('\nBulk operations:');
    result.forEach((value, key) => console.log(`  ${key} = ${value}`));

    // Replace a value
    const replaced = await client.replace('key1', 'newValue1');
    console.log(`\nreplaced key1: ${replaced}`);
    console.log(`get(key1) = ${await client.get('key1')}`);

    // Remove an entry
    const removed = await client.remove('key2');
    console.log(`\nremoved key2: ${removed}`);
    console.log(`get(key2) = ${await client.get('key2')}`);

    // Get cache size
    const size = await client.size();
    console.log(`\nCache size: ${size}`);

    // Clear the cache
    await client.clear();
    console.log(`Cache cleared. Size: ${await client.size()}`);
  } finally {
    await client.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
