package com.chen.memory.monitor.popup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PopupConfigTest {

    @Test
    fun defaultConfig_usesTopEnd() {
        val config = PopupConfig()

        assertEquals(PopupPosition.TOP_END, config.position)
        assertEquals(0, config.xOffsetPx)
        assertEquals(0, config.yOffsetPx)
    }

    @Test
    fun customLayout_withoutBinder_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            PopupConfig(customLayoutResId = 1234, customBinder = null)
        }
    }
}
