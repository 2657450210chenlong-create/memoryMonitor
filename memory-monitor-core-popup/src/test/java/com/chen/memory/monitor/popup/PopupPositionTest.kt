package com.chen.memory.monitor.popup

import android.view.Gravity
import org.junit.Assert.assertEquals
import org.junit.Test

class PopupPositionTest {

    @Test
    fun topEnd_mapsToExpectedGravity() {
        assertEquals(Gravity.TOP or Gravity.END, PopupPosition.TOP_END.toGravity())
    }

    @Test
    fun center_mapsToExpectedGravity() {
        assertEquals(Gravity.CENTER, PopupPosition.CENTER.toGravity())
    }

    @Test
    fun percentProgress_isClamped() {
        assertEquals(0, PopupTextFormatter.progressFromPercent(-5.0))
        assertEquals(46, PopupTextFormatter.progressFromPercent(45.6))
        assertEquals(100, PopupTextFormatter.progressFromPercent(199.0))
    }
}
