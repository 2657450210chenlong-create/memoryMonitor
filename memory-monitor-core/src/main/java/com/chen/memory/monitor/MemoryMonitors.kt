package com.chen.memory.monitor

object MemoryMonitors {
    @JvmStatic
    fun create(): MemoryMonitor = DefaultMemoryMonitor()

    @JvmStatic
    fun createAndRegister(): MemoryMonitor = create().also { monitor ->
        MemoryMonitorProvider.register(monitor)
    }
}
