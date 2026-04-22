package com.chen.memory.monitor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DefaultMemoryMonitor(
    private val collector: MemoryCollector = RuntimeMemoryCollector(),
    private val runtime: Runtime = Runtime.getRuntime(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : MemoryMonitor {

    private val lock = Any()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val scheduler = SamplingScheduler(scope, dispatcher)
    private val sampleStore = SampleStore()
    private val sampleFlow = MutableSharedFlow<MemorySample>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateFlow = MutableStateFlow(
        MemoryMonitorState(
            latestSample = null,
            history = emptyList(),
            currentUsedBytes = 0L,
            peakUsedBytes = 0L,
            maxMemoryBytes = runtime.maxMemory().coerceAtLeast(1L),
            usagePercent = 0.0,
            sampleCount = 0,
            isRunning = false
        )
    )

    @Volatile
    private var config: MemoryMonitorConfig? = null
    private var peakUsedBytes: Long = 0L

    override fun initialize(config: MemoryMonitorConfig) {
        synchronized(lock) {
            if (scheduler.isRunning()) {
                throw IllegalStateException("Cannot initialize while sampling. Call stop() first.")
            }
            this.config = config
            sampleStore.configure(config.maxSamples)
            sampleStore.clear()
            peakUsedBytes = 0L
            stateFlow.value = buildState(
                latestSample = null,
                history = emptyList(),
                isRunning = false
            )
        }
    }

    override fun start() {
        val currentConfig = synchronized(lock) {
            config ?: throw IllegalStateException(
                "MemoryMonitor is not initialized. Call initialize(...) before start()."
            )
        }

        synchronized(lock) {
            if (scheduler.isRunning()) {
                return
            }
            stateFlow.value = buildState(
                latestSample = sampleStore.latest(),
                history = sampleStore.snapshot(),
                isRunning = true
            )
        }

        scheduler.start(currentConfig.sampleIntervalMs) {
            val sample = collector.collect()
            synchronized(lock) {
                sampleStore.append(sample)
                val currentUsedBytes = totalUsedBytes(sample)
                peakUsedBytes = maxOf(peakUsedBytes, currentUsedBytes)
                stateFlow.value = buildState(
                    latestSample = sample,
                    history = sampleStore.snapshot(),
                    isRunning = true
                )
            }
            sampleFlow.tryEmit(sample)
        }
    }

    override fun stop() {
        scheduler.stop()
        synchronized(lock) {
            stateFlow.value = buildState(
                latestSample = sampleStore.latest(),
                history = sampleStore.snapshot(),
                isRunning = false
            )
        }
    }

    override fun clear() {
        synchronized(lock) {
            sampleStore.clear()
            peakUsedBytes = 0L
            stateFlow.value = buildState(
                latestSample = null,
                history = emptyList(),
                isRunning = scheduler.isRunning()
            )
        }
    }

    override fun isRunning(): Boolean = scheduler.isRunning()

    override fun latestSample(): MemorySample? = sampleStore.latest()

    override fun getHistory(): List<MemorySample> = sampleStore.snapshot()

    override fun observeSamples(): Flow<MemorySample> = sampleFlow.asSharedFlow()

    override fun currentState(): MemoryMonitorState = stateFlow.value

    override fun observeState(): Flow<MemoryMonitorState> = stateFlow.asStateFlow()

    private fun buildState(
        latestSample: MemorySample?,
        history: List<MemorySample>,
        isRunning: Boolean
    ): MemoryMonitorState {
        val currentUsedBytes = latestSample?.let(::totalUsedBytes) ?: 0L
        val maxMemoryBytes = runtime.maxMemory().coerceAtLeast(1L)
        val usagePercent = currentUsedBytes * 100.0 / maxMemoryBytes

        return MemoryMonitorState(
            latestSample = latestSample,
            history = history,
            currentUsedBytes = currentUsedBytes,
            peakUsedBytes = peakUsedBytes,
            maxMemoryBytes = maxMemoryBytes,
            usagePercent = usagePercent,
            sampleCount = history.size,
            isRunning = isRunning
        )
    }

    private fun totalUsedBytes(sample: MemorySample): Long {
        return sample.javaHeapUsedBytes + sample.nativeHeapAllocatedBytes
    }
}
