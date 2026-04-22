package com.chen.memory.monitor

import android.os.Debug

internal interface MemoryCollector {
    fun collect(): MemorySample
}

internal class RuntimeMemoryCollector(
    private val runtime: Runtime = Runtime.getRuntime(),
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : MemoryCollector {

    override fun collect(): MemorySample {
        val javaUsed = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        val nativeAllocated = Debug.getNativeHeapAllocatedSize().coerceAtLeast(0L)
        val nativeFree = Debug.getNativeHeapFreeSize().coerceAtLeast(0L)

        return MemorySample(
            timestampMs = nowProvider.invoke(),
            javaHeapUsedBytes = javaUsed,
            nativeHeapAllocatedBytes = nativeAllocated,
            nativeHeapFreeBytes = nativeFree
        )
    }
}
