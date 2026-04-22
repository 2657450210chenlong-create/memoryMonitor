package com.chen.memorymonitor

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chen.memory.monitor.MemoryMonitorConfig
import com.chen.memory.monitor.MemoryMonitors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemoryMonitorIntegrationTest {
    @Test
    fun samplingStopAndClear_workAsExpected() {
        val monitor = MemoryMonitors.create()
        monitor.initialize(
            MemoryMonitorConfig(
                sampleIntervalMs = 100L,
                maxSamples = 128
            )
        )

        monitor.start()
        Thread.sleep(1_100L)
        monitor.stop()

        val collectedCount = monitor.getHistory().size
        assertTrue(collectedCount in 8..14)

        val countAfterStop = monitor.getHistory().size
        Thread.sleep(300L)
        assertEquals(countAfterStop, monitor.getHistory().size)

        monitor.clear()
        assertTrue(monitor.getHistory().isEmpty())
    }
}
