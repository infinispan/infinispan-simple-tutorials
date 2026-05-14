'use strict';

const ispn = require('infinispan');
const protobuf = require('protobufjs');
const { setup, host, port, password } = require('../setup');

const personProto = `syntax = "proto3";
package tutorial;
/**
 * @TypeId(1000042)
 */
message Person {
    string firstName = 1;
    string lastName = 2;
    int32 bornYear = 3;
    string bornIn = 4;
}`;

const people = [
  {firstName: 'Hermione', lastName: 'Granger', bornYear: 1979, bornIn: 'London'},
  {firstName: 'Harry',    lastName: 'Potter',  bornYear: 1980, bornIn: 'Godric\'s Hollow'},
  {firstName: 'Ron',      lastName: 'Wesley',  bornYear: 1980, bornIn: 'London'},
  {firstName: 'Draco',    lastName: 'Malfoy',  bornYear: 1980, bornIn: 'London'}
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

  // Parse the proto schema
  const root = protobuf.parse(personProto).root;
  const Person = root.lookupType('.tutorial.Person');

  // Register the schema on the server
  const metaClient = await ispn.client({port, host}, {
    ...authOpts,
    cacheName: '___protobuf_metadata',
    dataFormat: {keyType: 'text/plain', valueType: 'text/plain'}
  });

  try {
    await metaClient.put('tutorial/Person.proto', personProto);
    console.log('Registered person.proto schema.');
  } finally {
    await metaClient.disconnect();
  }

  // Connect to the protostream cache
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
    client.registerProtostreamType('.tutorial.Person', 1000042);

    await client.clear();

    // Add people
    for (let i = 0; i < people.length; i++) {
      const message = Person.create(people[i]);
      await client.put(String(i), message);
    }
    console.log(`Added ${people.length} people.\n`);

    // Query all
    console.log('=== Query all ===');
    const all = await client.query({
      queryString: 'from tutorial.Person'
    });
    console.log(`Total results: ${all.length}`);
    all.forEach(p => console.log(`  ${p.firstName} ${p.lastName}`));

    // Query with filter
    console.log('\n=== Query: people with lastName = \'Granger\' ===');
    const grangers = await client.query({
      queryString: 'from tutorial.Person p where p.lastName = \'Granger\''
    });
    grangers.forEach(p => console.log(`  ${p.firstName} ${p.lastName}`));

    // Query with projection
    console.log('\n=== Query: projection of people born in London ===');
    const londonNames = await client.query({
      queryString: 'select p.firstName, p.lastName from tutorial.Person p where p.bornIn = \'London\''
    });
    londonNames.forEach(row => console.log(`  ${row[0]} ${row[1]}`));

    await client.clear();
    console.log('\nCache cleared.');
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
    await cleanupClient.remove('tutorial/Person.proto');
  } finally {
    await cleanupClient.disconnect();
  }
}

main().catch(err => { console.error(err); process.exit(1); });
