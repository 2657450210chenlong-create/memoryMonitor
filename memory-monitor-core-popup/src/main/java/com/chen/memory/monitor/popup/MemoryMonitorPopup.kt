package com.chen.memory.monitor.popup

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.chen.memory.monitor.MemoryMonitorProvider
import com.chen.memory.monitor.MemoryMonitorState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object MemoryMonitorPopup {
    private var popupWindow: PopupWindow? = null
    private var popupScope: CoroutineScope? = null
    private var destroyObserver: DefaultLifecycleObserver? = null
    private var lifecycleOwner: LifecycleOwner? = null

    @JvmStatic
    @Synchronized
    fun show(host: Activity, config: PopupConfig = PopupConfig()) {
        dismiss()

        val monitor = MemoryMonitorProvider.get()
        val contentView = createContentView(host, config)
        val popup = PopupWindow(contentView, WRAP_CONTENT, WRAP_CONTENT, false).apply {
            isOutsideTouchable = false
            isFocusable = false
            elevation = 12f
        }
        popupWindow = popup

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        popupScope = scope

        popup.setOnDismissListener {
            popupScope?.cancel()
            popupScope = null
            popupWindow = null
            removeLifecycleObserver()
        }

        bindState(contentView, config, monitor.currentState())
        popup.showAtLocation(
            host.window.decorView,
            config.position.toGravity(),
            config.xOffsetPx,
            config.yOffsetPx
        )

        scope.launch {
            monitor.observeState().collect { state ->
                bindState(contentView, config, state)
            }
        }

        attachLifecycleObserver(host)
    }

    @JvmStatic
    @Synchronized
    fun dismiss() {
        popupScope?.cancel()
        popupScope = null

        popupWindow?.setOnDismissListener(null)
        popupWindow?.dismiss()
        popupWindow = null
        removeLifecycleObserver()
    }

    private fun createContentView(activity: Activity, config: PopupConfig): View {
        val inflater = LayoutInflater.from(activity)
        return if (config.customLayoutResId == null) {
            inflater.inflate(R.layout.popup_memory_monitor_default, null, false)
        } else {
            inflater.inflate(config.customLayoutResId, null, false)
        }
    }

    private fun bindState(view: View, config: PopupConfig, state: MemoryMonitorState) {
        val binder = config.customBinder
        if (config.customLayoutResId != null && binder != null) {
            binder.invoke(view, state)
            return
        }

        view.findViewById<TextView>(R.id.tvPopupUsageValue).text =
            PopupTextFormatter.formatPercent(state.usagePercent)
        view.findViewById<ProgressBar>(R.id.progressPopupUsage).progress =
            PopupTextFormatter.progressFromPercent(state.usagePercent)
        view.findViewById<TextView>(R.id.tvPopupMemorySummary).text = view.context.getString(
            R.string.popup_summary_template,
            PopupTextFormatter.formatBytes(state.currentUsedBytes),
            PopupTextFormatter.formatBytes(state.maxMemoryBytes)
        )
    }

    private fun attachLifecycleObserver(host: Activity) {
        if (host !is LifecycleOwner) return

        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                dismiss()
            }
        }
        destroyObserver = observer
        lifecycleOwner = host
        host.lifecycle.addObserver(observer)
    }

    private fun removeLifecycleObserver() {
        val owner = lifecycleOwner ?: return
        val observer = destroyObserver ?: return
        owner.lifecycle.removeObserver(observer)
        destroyObserver = null
        lifecycleOwner = null
    }
}
