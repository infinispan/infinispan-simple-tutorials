'use strict';

const ispn = require('infinispan');

const host = process.env.ISPN_HOST || '127.0.0.1';
const port = parseInt(process.env.ISPN_PORT || '11222');
const password = process.env.ISPN_PASSWORD || 'password';

const CACHES = {
  test: '<distributed-cache><encoding media-type="application/x-protostream"/></distributed-cache>',
  textCache: '<distributed-cache><encoding><key media-type="text/plain"/><value media-type="text/plain"/></encoding></distributed-cache>',
  protoStreamCache: '<distributed-cache><encoding><key media-type="text/plain"/><value media-type="application/x-protostream"/></encoding></distributed-cache>'
};

async function setup() {
  const client = await ispn.client({port, host}, {
    clientIntelligence: 'BASIC',
    authentication: {
      enabled: true,
      saslMechanism: 'SCRAM-SHA-256',
      userName: 'admin',
      password
    }
  });

  for (const [name, config] of Object.entries(CACHES)) {
    await client.admin.getOrCreateCache(name, config);
  }

  await client.disconnect();
}

module.exports = { setup, host, port, password };
