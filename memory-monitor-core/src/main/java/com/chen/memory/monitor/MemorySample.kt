package com.chen.memory.monitor

data class MemorySample(
    val timestampMs: Long,
    val javaHeapUsedBytes: Long,
    val nativeHeapAllocatedBytes: Long,
    val nativeHeapFreeBytes: Long
)
