package com.chen.memory.monitor.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ServiceConfigTest {

    @Test
    fun defaultConfig_hasOneSecondThrottle() {
        val config = ServiceConfig()

        assertEquals(1000L, config.throttleMs)
    }

    @Test
    fun invalidThrottle_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            ServiceConfig(throttleMs = 0L)
        }
    }
}
