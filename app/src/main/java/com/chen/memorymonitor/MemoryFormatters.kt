package com.chen.memorymonitor

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object MemoryFormatters {
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun formatTimestamp(timestampMs: Long): String {
        return timeFormatter.format(Date(timestampMs))
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024L) return "$bytes B"
        val unitIndex = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val safeIndex = unitIndex.coerceIn(0, units.lastIndex)
        val scaled = bytes / 1024.0.pow(safeIndex.toDouble())
        return String.format(Locale.getDefault(), "%.2f %s", scaled, units[safeIndex])
    }

    fun formatPercent(numerator: Long, denominator: Long): String {
        if (denominator <= 0L) return "0.00%"
        val ratio = numerator.toDouble() * 100.0 / denominator.toDouble()
        return formatPercent(ratio)
    }

    fun formatPercent(percent: Double): String {
        return String.format(Locale.getDefault(), "%.2f%%", percent)
    }
}
