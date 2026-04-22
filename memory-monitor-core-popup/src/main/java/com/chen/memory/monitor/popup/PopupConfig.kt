package com.chen.memory.monitor.popup

import android.view.View
import com.chen.memory.monitor.MemoryMonitorState

data class PopupConfig(
    val position: PopupPosition = PopupPosition.TOP_END,
    val xOffsetPx: Int = 0,
    val yOffsetPx: Int = 0,
    val customLayoutResId: Int? = null,
    val customBinder: ((View, MemoryMonitorState) -> Unit)? = null
) {
    init {
        require(customLayoutResId == null || customBinder != null) {
            "customBinder must be provided when customLayoutResId is set."
        }
    }
}
