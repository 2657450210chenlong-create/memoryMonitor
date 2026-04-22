package com.chen.memory.monitor.service

import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceTextFormatterTest {

    @Test
    fun progress_isClampedToRange() {
        assertEquals(0, ServiceTextFormatter.progressFromUsagePercent(-12.0))
        assertEquals(50, ServiceTextFormatter.progressFromUsagePercent(50.1))
        assertEquals(100, ServiceTextFormatter.progressFromUsagePercent(180.0))
    }
}
