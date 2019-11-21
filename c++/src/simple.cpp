#include "infinispan/hotrod/ConfigurationBuilder.h"
#include "infinispan/hotrod/RemoteCacheManager.h"
#include "infinispan/hotrod/RemoteCache.h"
#include "infinispan/hotrod/Version.h"

#include <stdlib.h>
#include <iostream>

using namespace infinispan::hotrod;

int main(int argc, char** argv) {
    // Create a configuration for a locally-running server
    ConfigurationBuilder builder;
    builder.addServer().host(argc > 1 ? argv[1] : "127.0.0.1").port(argc > 2 ? atoi(argv[2]) : 11222);
    // Initialize the remote cache manager
    RemoteCacheManager cacheManager(builder.build(), false);
    // Obtain the remote cache
    RemoteCache<std::string, std::string> cache = cacheManager.getCache<std::string, std::string>();
    // Connect to the server
    cacheManager.start();
    // Store a value
    cache.put("key", "value");
    // Retrieve the value and print it out
    std::cout << "key = " << *cache.get("key") << std::endl;
    // Stop the cache manager and release all resources
    cacheManager.stop();
    return 0;
}
