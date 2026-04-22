package com.chen.memory.monitor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SamplingScheduler(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) {
    private val lock = Any()
    private var samplingJob: Job? = null

    fun start(intervalMs: Long, task: () -> Unit) {
        synchronized(lock) {
            if (samplingJob?.isActive == true) {
                return
            }
            samplingJob = scope.launch(dispatcher) {
                while (isActive) {
                    task.invoke()
                    delay(intervalMs)
                }
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            samplingJob?.cancel()
            samplingJob = null
        }
    }

    fun isRunning(): Boolean = synchronized(lock) {
        samplingJob?.isActive == true
    }
}
