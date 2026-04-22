package com.chen.memory.monitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MemoryMonitorProviderTest {

    @Before
    fun setUp() {
        MemoryMonitorProvider.clearForTests()
    }

    @Test
    fun register_firstTime_succeeds() {
        val monitor = FakeMemoryMonitor()

        MemoryMonitorProvider.register(monitor)

        assertTrue(MemoryMonitorProvider.isRegistered())
        assertSame(monitor, MemoryMonitorProvider.get())
    }

    @Test
    fun register_sameInstanceAgain_isNoOp() {
        val monitor = FakeMemoryMonitor()
        MemoryMonitorProvider.register(monitor)

        MemoryMonitorProvider.register(monitor)

        assertSame(monitor, MemoryMonitorProvider.get())
    }

    @Test
    fun register_differentInstance_throws() {
        MemoryMonitorProvider.register(FakeMemoryMonitor())

        assertThrows(IllegalStateException::class.java) {
            MemoryMonitorProvider.register(FakeMemoryMonitor())
        }
    }

    @Test
    fun get_withoutRegister_throws() {
        assertThrows(IllegalStateException::class.java) {
            MemoryMonitorProvider.get()
        }
    }
}

private class FakeMemoryMonitor : MemoryMonitor {
    override fun initialize(config: MemoryMonitorConfig) = Unit
    override fun start() = Unit
    override fun stop() = Unit
    override fun clear() = Unit
    override fun isRunning(): Boolean = false
    override fun latestSample(): MemorySample? = null
    override fun getHistory(): List<MemorySample> = emptyList()
    override fun observeSamples(): Flow<MemorySample> = emptyFlow()
    override fun currentState(): MemoryMonitorState = MemoryMonitorState(
        latestSample = null,
        history = emptyList(),
        currentUsedBytes = 0L,
        peakUsedBytes = 0L,
        maxMemoryBytes = 1L,
        usagePercent = 0.0,
        sampleCount = 0,
        isRunning = false
    )

    override fun observeState(): Flow<MemoryMonitorState> = emptyFlow()
}
