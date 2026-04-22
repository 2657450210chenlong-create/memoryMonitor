package com.chen.memory.monitor.service

data class ServiceConfig(
    val channelId: String = "memory_monitor_channel",
    val channelName: String = "Memory Monitor",
    val notificationId: Int = 9168,
    val throttleMs: Long = 1000L,
    val smallIconRes: Int = android.R.drawable.stat_notify_sync
) {
    init {
        require(throttleMs > 0L) { "throttleMs must be greater than 0." }
    }
}
