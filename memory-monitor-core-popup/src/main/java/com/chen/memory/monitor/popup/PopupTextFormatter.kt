package com.chen.memory.monitor.popup

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

internal object PopupTextFormatter {
    fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes B"
        val unitIndex = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val safeIndex = unitIndex.coerceIn(0, units.lastIndex)
        val scaled = bytes / 1024.0.pow(safeIndex.toDouble())
        return String.format(Locale.getDefault(), "%.2f %s", scaled, units[safeIndex])
    }

    fun formatPercent(percent: Double): String {
        return String.format(Locale.getDefault(), "%.2f%%", percent)
    }

    fun progressFromPercent(percent: Double): Int {
        return percent.coerceIn(0.0, 100.0).roundToInt()
    }
}
