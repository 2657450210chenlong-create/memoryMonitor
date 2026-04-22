package com.chen.memorymonitor

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun formatter_displaysBytes() {
        assertEquals("512 B", MemoryFormatters.formatBytes(512))
    }

    @Test
    fun formatter_displaysPercent() {
        assertEquals("25.00%", MemoryFormatters.formatPercent(256, 1024))
    }
}
