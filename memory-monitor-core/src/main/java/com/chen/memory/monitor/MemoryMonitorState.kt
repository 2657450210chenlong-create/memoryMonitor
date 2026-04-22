package com.chen.memory.monitor

data class MemoryMonitorState(
    val latestSample: MemorySample?,
    val history: List<MemorySample>,
    val currentUsedBytes: Long,
    val peakUsedBytes: Long,
    val maxMemoryBytes: Long,
    val usagePercent: Double,
    val sampleCount: Int,
    val isRunning: Boolean
)
