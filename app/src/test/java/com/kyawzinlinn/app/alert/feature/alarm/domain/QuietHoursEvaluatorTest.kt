package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuietHoursEvaluatorTest {
    private fun minutes(h: Int, m: Int = 0) = h * 60 + m

    @Test fun `disabled is never quiet`() {
        assertFalse(QuietHoursEvaluator.isQuiet(QuietHours(enabled = false), 1, minutes(23)))
    }

    @Test fun `same day window includes start and excludes end`() {
        val q = QuietHours(true, minutes(9), minutes(17))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 3, minutes(9)))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 3, minutes(16, 59)))
        assertFalse(QuietHoursEvaluator.isQuiet(q, 3, minutes(17)))
        assertFalse(QuietHoursEvaluator.isQuiet(q, 3, minutes(8, 59)))
    }

    @Test fun `overnight window covers late evening and early morning`() {
        val q = QuietHours(true, minutes(22), minutes(7))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 2, minutes(23)))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 3, minutes(6, 30))) // belongs to window started Tuesday
        assertFalse(QuietHoursEvaluator.isQuiet(q, 3, minutes(12)))
        assertFalse(QuietHoursEvaluator.isQuiet(q, 3, minutes(7)))
    }

    @Test fun `overnight window honours the start day only`() {
        val q = QuietHours(true, minutes(22), minutes(7), days = setOf(5)) // Friday night
        assertTrue(QuietHoursEvaluator.isQuiet(q, 5, minutes(23)))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 6, minutes(3)))  // Saturday early morning
        assertFalse(QuietHoursEvaluator.isQuiet(q, 5, minutes(3))) // Friday early morning: Thursday not selected
        assertFalse(QuietHoursEvaluator.isQuiet(q, 6, minutes(23)))
    }

    @Test fun `sunday window wraps into monday`() {
        val q = QuietHours(true, minutes(22), minutes(7), days = setOf(7))
        assertTrue(QuietHoursEvaluator.isQuiet(q, 1, minutes(2)))
    }

    @Test fun `empty days or equal start and end is never quiet`() {
        assertFalse(QuietHoursEvaluator.isQuiet(QuietHours(true, 0, 600, days = emptySet()), 1, 100))
        assertFalse(QuietHoursEvaluator.isQuiet(QuietHours(true, 600, 600), 1, 600))
    }
}
