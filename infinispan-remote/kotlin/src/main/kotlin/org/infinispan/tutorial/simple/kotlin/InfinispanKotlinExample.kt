package org.infinispan.tutorial.simple.kotlin

import org.infinispan.client.hotrod.RemoteCache
import org.infinispan.client.hotrod.RemoteCacheManager
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME

object InfinispanKotlinExample {
    private lateinit var cacheManager: RemoteCacheManager
    private lateinit var cache: RemoteCache<String, String>

    @JvmStatic
    fun main(args: Array<String>) {
        connectToInfinispan()
        manipulateCache()
        disconnect()
    }

    private fun manipulateCache() {
        // Store a value
        cache.put("key", "value")
        // Retrieve the value and print it out
        println("key = ${cache["key"]}")
    }

    private fun connectToInfinispan() {
        // Connect to the server
        cacheManager = TutorialsConnectorHelper.connect()
        // Obtain the remote cache
        cache = cacheManager.getCache(TUTORIAL_CACHE_NAME)
    }

    private fun disconnect() {
        // Stop the cache manager and release all resources
        TutorialsConnectorHelper.stop(cacheManager)
    }
}
