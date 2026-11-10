package com.kyawzinlinn.app.alert.feature.alarm.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSettingsTest {
    @Test fun `sanitized clamps every value into its allowed range`() {
        val result = AlarmSettings(
            volumePercent = 500, durationSeconds = 0, repeatCount = 99,
            repeatIntervalSeconds = 1, cooldownSeconds = -5,
        ).sanitized()
        assertEquals(100, result.volumePercent)
        assertEquals(3, result.durationSeconds)
        assertEquals(10, result.repeatCount)
        assertEquals(5, result.repeatIntervalSeconds)
        assertEquals(0, result.cooldownSeconds)
    }

    @Test fun `defaults are already valid`() {
        assertEquals(AlarmSettings(), AlarmSettings().sanitized())
    }
}
