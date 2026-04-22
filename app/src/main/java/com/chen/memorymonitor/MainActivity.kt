package com.chen.memorymonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chen.memory.monitor.MemoryMonitor
import com.chen.memory.monitor.MemoryMonitorConfig
import com.chen.memory.monitor.MemoryMonitors
import com.chen.memory.monitor.MemoryMonitorProvider
import com.chen.memory.monitor.MemoryMonitorState
import com.chen.memory.monitor.popup.MemoryMonitorPopup
import com.chen.memory.monitor.popup.PopupConfig
import com.chen.memory.monitor.popup.PopupPosition
import com.chen.memory.monitor.service.MemoryMonitorForegroundController
import com.chen.memory.monitor.service.ServiceConfig
import com.chen.memorymonitor.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val monitor: MemoryMonitor by lazy {
        if (MemoryMonitorProvider.isRegistered()) {
            MemoryMonitorProvider.get()
        } else {
            MemoryMonitors.create().also { created ->
                MemoryMonitorProvider.register(created)
            }
        }
    }
    private val sampleAdapter = SampleAdapter()
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startForegroundServiceSafely()
            } else {
                showToast(getString(R.string.toast_notification_permission_needed))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUi()
        observeMonitorState()
    }

    override fun onDestroy() {
        MemoryMonitorPopup.dismiss()
        super.onDestroy()
    }

    private fun setupUi() {
        binding.etSampleInterval.setText("100")
        binding.etMaxSamples.setText("600")

        binding.recyclerSamples.layoutManager = LinearLayoutManager(this)
        binding.recyclerSamples.adapter = sampleAdapter

        binding.buttonInit.setOnClickListener { initializeMonitor() }
        binding.buttonStart.setOnClickListener {
            runCatching { monitor.start() }
                .onSuccess {
                    updateStatus(getString(R.string.status_running))
                    showToast(getString(R.string.toast_started))
                }
                .onFailure {
                    showToast(it.message ?: getString(R.string.error_unknown))
                }
        }
        binding.buttonStop.setOnClickListener {
            monitor.stop()
            updateStatus(getString(R.string.status_stopped))
            showToast(getString(R.string.toast_stopped))
        }
        binding.buttonClear.setOnClickListener {
            monitor.clear()
            updateStatus(getString(R.string.status_cleared))
            showToast(getString(R.string.toast_cleared))
        }
        binding.buttonShowPopup.setOnClickListener {
            runCatching {
                MemoryMonitorPopup.show(
                    host = this,
                    config = PopupConfig(position = PopupPosition.TOP_END)
                )
            }.onSuccess {
                showToast(getString(R.string.toast_popup_shown))
            }.onFailure { throwable ->
                showToast(throwable.message ?: getString(R.string.error_unknown))
            }
        }
        binding.buttonHidePopup.setOnClickListener {
            MemoryMonitorPopup.dismiss()
            showToast(getString(R.string.toast_popup_hidden))
        }
        binding.buttonStartService.setOnClickListener {
            ensureNotificationPermissionAndStartService()
        }
        binding.buttonStopService.setOnClickListener {
            MemoryMonitorForegroundController.stop(this)
            showToast(getString(R.string.toast_service_stopped))
        }
    }

    private fun initializeMonitor() {
        val intervalMs = binding.etSampleInterval.text.toString().trim().toLongOrNull()
        val maxSamples = binding.etMaxSamples.text.toString().trim().toIntOrNull()

        if (intervalMs == null || intervalMs <= 0L) {
            showToast(getString(R.string.error_interval_invalid))
            return
        }
        if (maxSamples == null || maxSamples <= 0) {
            showToast(getString(R.string.error_max_samples_invalid))
            return
        }

        monitor.stop()
        monitor.initialize(
            MemoryMonitorConfig(
                sampleIntervalMs = intervalMs,
                maxSamples = maxSamples
            )
        )

        updateStatus(getString(R.string.status_initialized, intervalMs, maxSamples))
        showToast(getString(R.string.toast_initialized))
    }

    private fun observeMonitorState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                monitor.observeState().collect { state ->
                    updateStats(state)
                    sampleAdapter.submitList(state.history.asReversed())
                }
            }
        }
    }

    private fun updateStats(state: MemoryMonitorState) {
        binding.tvCurrentValue.text = MemoryFormatters.formatBytes(state.currentUsedBytes)
        binding.tvPeakValue.text = MemoryFormatters.formatBytes(state.peakUsedBytes)
        binding.tvMaxValue.text = MemoryFormatters.formatBytes(state.maxMemoryBytes)
        binding.tvUsageValue.text = MemoryFormatters.formatPercent(state.usagePercent)
        binding.tvCountValue.text = state.sampleCount.toString()
    }

    private fun updateStatus(message: String) {
        binding.tvStatusValue.text = message
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun ensureNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            showToast(getString(R.string.toast_notification_disabled))
            return
        }

        startForegroundServiceSafely()
    }

    private fun startForegroundServiceSafely() {
        runCatching {
            MemoryMonitorForegroundController.start(
                context = this,
                config = ServiceConfig(smallIconRes = android.R.drawable.stat_notify_sync)
            )
        }.onSuccess {
            showToast(getString(R.string.toast_service_started))
        }.onFailure { throwable ->
            showToast(throwable.message ?: getString(R.string.error_unknown))
        }
    }
}
