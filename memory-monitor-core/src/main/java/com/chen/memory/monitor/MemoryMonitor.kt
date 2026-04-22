package com.chen.memory.monitor

import kotlinx.coroutines.flow.Flow

interface MemoryMonitor {
    fun initialize(config: MemoryMonitorConfig)
    fun start()
    fun stop()
    fun clear()
    fun isRunning(): Boolean
    fun latestSample(): MemorySample?
    fun getHistory(): List<MemorySample>
    fun observeSamples(): Flow<MemorySample>
    fun currentState(): MemoryMonitorState
    fun observeState(): Flow<MemoryMonitorState>
}
