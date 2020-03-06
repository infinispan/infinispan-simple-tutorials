var infinispan = require('infinispan');

var connected = infinispan.client({port: 11222, host: '127.0.0.1'},  {cacheName: 'javascriptTutorial'});
connected.then(function(client) {
  console.log("Connected");
  var putGetPromise = client.put('key', 'value').then(function () {
    return client.get('key').then(function (value) {
      console.log('key = ' + value);
    })
  });

  return putGetPromise.finally(function() {
    // Regardless of the result, disconnect client
    return client.disconnect().then(function() { console.log("Disconnected") });
  });
});
