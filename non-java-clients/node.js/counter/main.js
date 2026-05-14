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
    // Create a strong counter
    await client.counterCreate('visits', {
      type: 'strong',
      initialValue: 0,
      storage: 'volatile'
    });
    console.log('Created strong counter "visits".');

    // Increment the counter
    let val = await client.counterAddAndGet('visits', 1);
    console.log(`After +1: ${val}`);
    val = await client.counterAddAndGet('visits', 5);
    console.log(`After +5: ${val}`);

    // Get current value
    val = await client.counterGet('visits');
    console.log(`Current value: ${val}`);

    // Compare and swap
    const prev = await client.counterCompareAndSwap('visits', 6, 100);
    console.log(`\nCAS(expect=6, update=100): previous=${prev}`);
    console.log(`After CAS: ${await client.counterGet('visits')}`);

    // Failed CAS (expected value doesn't match)
    const prev2 = await client.counterCompareAndSwap('visits', 6, 200);
    console.log(`\nCAS(expect=6, update=200): previous=${prev2}`);
    console.log(`After failed CAS: ${await client.counterGet('visits')}`);

    // Reset to initial value
    await client.counterReset('visits');
    console.log(`\nAfter reset: ${await client.counterGet('visits')}`);

    // Create a weak counter
    await client.counterCreate('page-views', {
      type: 'weak',
      initialValue: 0,
      concurrencyLevel: 4,
      storage: 'volatile'
    });
    console.log('\nCreated weak counter "page-views".');

    // Increment weak counter
    await client.counterAddAndGet('page-views', 1);
    await client.counterAddAndGet('page-views', 1);
    await client.counterAddAndGet('page-views', 1);
    val = await client.counterGet('page-views');
    console.log(`page-views after 3 increments: ${val}`);

    // Get counter configuration
    const config = await client.counterGetConfiguration('visits');
    console.log(`\n"visits" config: type=${config.type}, initialValue=${config.initialValue}`);

    // Clean up
    await client.counterRemove('visits');
    await client.counterRemove('page-views');
    console.log('\nCounters removed.');
  } finally {
    await client.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
