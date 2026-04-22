package com.chen.memory.monitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.chen.memory.monitor.MemoryMonitor
import com.chen.memory.monitor.MemoryMonitorProvider
import com.chen.memory.monitor.MemoryMonitorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MemoryMonitorForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var stateJob: Job? = null
    private var monitor: MemoryMonitor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startOrUpdateTracking()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopTrackingOnly()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startOrUpdateTracking() {
        val config = MemoryMonitorForegroundController.currentConfig()
        val registeredMonitor = runCatching { MemoryMonitorProvider.get() }
            .getOrElse { throwable ->
                Log.e(TAG, "Memory monitor is not registered. Stop service.", throwable)
                stopTrackingAndService()
                return
            }
        monitor = registeredMonitor

        createChannelIfNeeded(config)
        val initialState = registeredMonitor.currentState()
        startForeground(config.notificationId, buildNotification(initialState, config))
        startStateCollection(registeredMonitor, config)
    }

    private fun startStateCollection(monitor: MemoryMonitor, config: ServiceConfig) {
        stateJob?.cancel()
        stateJob = serviceScope.launch {
            var lastUpdateElapsed = 0L
            monitor.observeState().collect { state ->
                val now = SystemClock.elapsedRealtime()
                if (now - lastUpdateElapsed < config.throttleMs) {
                    return@collect
                }
                lastUpdateElapsed = now
                notifyState(state, config)
            }
        }
    }

    private fun notifyState(state: MemoryMonitorState, config: ServiceConfig) {
        val notification = buildNotification(state, config)
        notificationManager().notify(config.notificationId, notification)
    }

    private fun buildNotification(state: MemoryMonitorState, config: ServiceConfig): Notification {
        val progress = ServiceTextFormatter.progressFromUsagePercent(state.usagePercent)
        val title = getString(
            R.string.notification_title_template,
            ServiceTextFormatter.formatBytes(state.currentUsedBytes),
            ServiceTextFormatter.formatBytes(state.maxMemoryBytes)
        )
        val text = getString(
            R.string.notification_text_template,
            ServiceTextFormatter.formatBytes(state.peakUsedBytes),
            ServiceTextFormatter.formatPercent(state.usagePercent)
        )

        return NotificationCompat.Builder(this, config.channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(config.smallIconRes)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannelIfNeeded(config: ServiceConfig) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            config.channelId,
            config.channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        notificationManager().createNotificationChannel(channel)
    }

    private fun notificationManager(): NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun stopTrackingOnly() {
        stateJob?.cancel()
        stateJob = null
        monitor = null
    }

    private fun stopTrackingAndService() {
        stopTrackingOnly()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ACTION_START_OR_UPDATE = "com.chen.memory.monitor.service.action.START_OR_UPDATE"

        private const val TAG = "MemoryMonitorService"
    }
}
