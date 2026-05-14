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

async function main() {
  await setup();

  // Connect with text/plain encoding
  const textClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'textCache',
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'text/plain'
    }
  });

  try {
    console.log('== Cache with text/plain encoding ==');
    await textClient.put('greeting', 'Hello, Infinispan!');
    const textVal = await textClient.get('greeting');
    console.log(`get(greeting) = ${textVal}`);
    await textClient.clear();
  } finally {
    await textClient.disconnect();
  }

  // Connect with application/json encoding
  const jsonClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: 'textCache',
    dataFormat: {
      keyType: 'text/plain',
      valueType: 'application/json'
    }
  });

  try {
    console.log('\n== Cache with application/json encoding ==');
    const jsonValue = JSON.stringify({project: 'Infinispan', language: 'JavaScript'});
    await jsonClient.put('info', jsonValue);
    const jsonVal = await jsonClient.get('info');
    console.log(`get(info) = ${jsonVal}`);

    const parsed = JSON.parse(jsonVal);
    console.log(`  project  = ${parsed.project}`);
    console.log(`  language = ${parsed.language}`);
    await jsonClient.clear();
  } finally {
    await jsonClient.disconnect();
  }

  console.log('\nDone.');
}

main().catch(err => { console.error(err); process.exit(1); });
