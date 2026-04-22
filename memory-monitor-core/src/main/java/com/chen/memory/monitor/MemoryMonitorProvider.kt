package com.chen.memory.monitor

import androidx.annotation.VisibleForTesting

object MemoryMonitorProvider {
    private val lock = Any()

    @Volatile
    private var monitor: MemoryMonitor? = null

    @JvmStatic
    fun register(monitor: MemoryMonitor) {
        synchronized(lock) {
            val existing = this.monitor
            if (existing == null) {
                this.monitor = monitor
                return
            }
            if (existing !== monitor) {
                throw IllegalStateException(
                    "MemoryMonitorProvider already registered with a different instance."
                )
            }
        }
    }

    @JvmStatic
    fun get(): MemoryMonitor {
        return monitor ?: throw IllegalStateException(
            "MemoryMonitorProvider is not registered. Register a monitor first."
        )
    }

    @JvmStatic
    fun isRegistered(): Boolean = monitor != null

    @VisibleForTesting
    @JvmStatic
    fun clearForTests() {
        synchronized(lock) {
            monitor = null
        }
    }
}
