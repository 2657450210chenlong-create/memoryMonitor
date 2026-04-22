package com.chen.memory.monitor

data class MemoryMonitorConfig(
    val sampleIntervalMs: Long = 100L,
    val maxSamples: Int = 600
) {
    init {
        require(sampleIntervalMs > 0L) {
            "sampleIntervalMs must be greater than 0."
        }
        require(maxSamples > 0) {
            "maxSamples must be greater than 0."
        }
    }
}
