package com.chen.memory.monitor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultMemoryMonitorTest {

    @Test
    fun start_withoutInitialize_throws() {
        val monitor = DefaultMemoryMonitor(collector = FakeCollector())

        assertThrows(IllegalStateException::class.java) {
            monitor.start()
        }
    }

    @Test
    fun startStop_areIdempotent_andStateTransitionsAreCorrect() {
        val monitor = DefaultMemoryMonitor(collector = FakeCollector())
        monitor.initialize(MemoryMonitorConfig(sampleIntervalMs = 20L, maxSamples = 10))

        monitor.start()
        monitor.start()
        assertTrue(monitor.isRunning())

        Thread.sleep(140L)

        monitor.stop()
        monitor.stop()
        assertFalse(monitor.isRunning())
    }

    @Test
    fun collectedSamples_haveNonNegativeValues_andIncreasingTimestamps() {
        val monitor = DefaultMemoryMonitor(collector = FakeCollector())
        monitor.initialize(MemoryMonitorConfig(sampleIntervalMs = 10L, maxSamples = 20))
        monitor.start()
        Thread.sleep(80L)
        monitor.stop()

        val history = monitor.getHistory()
        assertTrue(history.size >= 4)
        assertTrue(history.all { sample ->
            sample.javaHeapUsedBytes >= 0L &&
                sample.nativeHeapAllocatedBytes >= 0L &&
                sample.nativeHeapFreeBytes >= 0L
        })

        history.zipWithNext { previous, current ->
            assertTrue(current.timestampMs >= previous.timestampMs)
        }

        val state = monitor.currentState()
        assertNotNull(state.latestSample)
        assertTrue(state.maxMemoryBytes > 0L)
        assertTrue(state.currentUsedBytes >= 0L)
        assertTrue(state.peakUsedBytes >= state.currentUsedBytes)
        assertTrue(state.usagePercent >= 0.0)
        assertEquals(history.size, state.sampleCount)
    }

    @Test
    fun clear_removesAllHistory() {
        val monitor = DefaultMemoryMonitor(collector = FakeCollector())
        monitor.initialize(MemoryMonitorConfig(sampleIntervalMs = 10L, maxSamples = 20))
        monitor.start()
        Thread.sleep(50L)
        monitor.stop()
        assertTrue(monitor.getHistory().isNotEmpty())

        monitor.clear()

        assertTrue(monitor.getHistory().isEmpty())
        assertEquals(null, monitor.latestSample())
        val clearedState = monitor.currentState()
        assertEquals(0L, clearedState.currentUsedBytes)
        assertEquals(0L, clearedState.peakUsedBytes)
        assertEquals(0, clearedState.sampleCount)
    }
}

private class FakeCollector : MemoryCollector {
    private var counter: Long = 0L

    override fun collect(): MemorySample {
        counter += 1
        return MemorySample(
            timestampMs = counter,
            javaHeapUsedBytes = counter,
            nativeHeapAllocatedBytes = counter,
            nativeHeapFreeBytes = counter
        )
    }
}
