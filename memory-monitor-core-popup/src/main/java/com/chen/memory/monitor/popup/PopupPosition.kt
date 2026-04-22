package com.chen.memory.monitor.popup

import android.view.Gravity

enum class PopupPosition {
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END,
    CENTER
}

internal fun PopupPosition.toGravity(): Int = when (this) {
    PopupPosition.TOP_START -> Gravity.TOP or Gravity.START
    PopupPosition.TOP_END -> Gravity.TOP or Gravity.END
    PopupPosition.BOTTOM_START -> Gravity.BOTTOM or Gravity.START
    PopupPosition.BOTTOM_END -> Gravity.BOTTOM or Gravity.END
    PopupPosition.CENTER -> Gravity.CENTER
}
