package org.infinispan.tutorial.simple.kotlin

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class InfinispanKotlinExampleTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun start() {
            InfinispanKotlinExample.connectToInfinispan()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            InfinispanKotlinExample.disconnect()
        }
    }

    @Test
    fun testRemoteCache() {
        assertNotNull(InfinispanKotlinExample.cache)

        InfinispanKotlinExample.manipulateCache()

        assertEquals("value", InfinispanKotlinExample.cache["key"])
    }
}
