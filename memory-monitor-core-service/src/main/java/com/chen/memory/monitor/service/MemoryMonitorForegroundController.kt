package com.chen.memory.monitor.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.chen.memory.monitor.MemoryMonitorProvider

object MemoryMonitorForegroundController {
    @Volatile
    private var latestConfig: ServiceConfig = ServiceConfig()

    @JvmStatic
    fun start(context: Context, config: ServiceConfig = ServiceConfig()) {
        check(MemoryMonitorProvider.isRegistered()) {
            "MemoryMonitorProvider is not registered. Register monitor before starting service."
        }
        latestConfig = config
        val intent = Intent(context.applicationContext, MemoryMonitorForegroundService::class.java)
            .setAction(MemoryMonitorForegroundService.ACTION_START_OR_UPDATE)
        ContextCompat.startForegroundService(context.applicationContext, intent)
    }

    @JvmStatic
    fun stop(context: Context) {
        val intent = Intent(context.applicationContext, MemoryMonitorForegroundService::class.java)
        context.applicationContext.stopService(intent)
    }

    internal fun currentConfig(): ServiceConfig = latestConfig
}
