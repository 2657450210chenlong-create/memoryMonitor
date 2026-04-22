package com.chen.memory.monitor

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleStoreTest {

    @Test
    fun append_whenExceedingMaxSamples_keepsLatestRecords() {
        val store = SampleStore()
        store.configure(maxSamples = 3)

        repeat(5) { index ->
            store.append(
                MemorySample(
                    timestampMs = index.toLong(),
                    javaHeapUsedBytes = index.toLong(),
                    nativeHeapAllocatedBytes = index.toLong(),
                    nativeHeapFreeBytes = index.toLong()
                )
            )
        }

        val snapshot = store.snapshot()
        assertEquals(3, snapshot.size)
        assertEquals(2L, snapshot.first().timestampMs)
        assertEquals(4L, snapshot.last().timestampMs)
    }
}
