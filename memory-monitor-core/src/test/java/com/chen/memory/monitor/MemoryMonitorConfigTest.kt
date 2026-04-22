package com.chen.memory.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MemoryMonitorConfigTest {

    @Test
    fun createConfig_withValidValues() {
        val config = MemoryMonitorConfig(sampleIntervalMs = 50L, maxSamples = 256)

        assertEquals(50L, config.sampleIntervalMs)
        assertEquals(256, config.maxSamples)
    }

    @Test
    fun createConfig_withInvalidInterval_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            MemoryMonitorConfig(sampleIntervalMs = 0L, maxSamples = 10)
        }
    }

    @Test
    fun createConfig_withInvalidMaxSamples_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            MemoryMonitorConfig(sampleIntervalMs = 100L, maxSamples = 0)
        }
    }
}
