'use strict';

const ispn = require('infinispan');
const { setup, host, port, password } = require('../setup');

async function main() {
  await setup();

  // Connect with SCRAM-SHA-256 credentials
  console.log('Connecting with SCRAM-SHA-256 mechanism...');
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
    await client.put('key', 'value');
    const val = await client.get('key');
    console.log(`SCRAM-SHA-256 auth successful: ${val}`);
    await client.clear();
  } finally {
    await client.disconnect();
  }

  // Connect with SCRAM-SHA-512 mechanism
  console.log('\nConnecting with SCRAM-SHA-512 mechanism...');
  const scramClient = await ispn.client({port, host}, {
    cacheName: 'test',
    authentication: {
      enabled: true,
      saslMechanism: 'SCRAM-SHA-512',
      userName: 'admin',
      password
    }
  });

  try {
    await scramClient.put('scram-key', 'scram-value');
    const scramVal = await scramClient.get('scram-key');
    console.log(`SCRAM-SHA-512 auth successful: ${scramVal}`);
    await scramClient.clear();
  } finally {
    await scramClient.disconnect();
  }

  console.log('\nDone.');
}

main().catch(err => { console.error(err); process.exit(1); });
