package com.chen.memory.monitor

internal class SampleStore {
    private val lock = Any()
    private val samples = ArrayDeque<MemorySample>()
    private var maxSamples: Int = 1

    fun configure(maxSamples: Int) {
        require(maxSamples > 0) { "maxSamples must be greater than 0." }
        synchronized(lock) {
            this.maxSamples = maxSamples
            trimIfNeeded()
        }
    }

    fun append(sample: MemorySample) {
        synchronized(lock) {
            samples.addLast(sample)
            trimIfNeeded()
        }
    }

    fun clear() {
        synchronized(lock) {
            samples.clear()
        }
    }

    fun latest(): MemorySample? = synchronized(lock) {
        samples.lastOrNull()
    }

    fun snapshot(): List<MemorySample> = synchronized(lock) {
        samples.toList()
    }

    private fun trimIfNeeded() {
        while (samples.size > maxSamples) {
            samples.removeFirst()
        }
    }
}
