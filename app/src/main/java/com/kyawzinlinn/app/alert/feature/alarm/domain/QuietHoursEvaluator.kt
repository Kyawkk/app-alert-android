package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours

object QuietHoursEvaluator {
    // a window that ends before it starts crosses midnight and belongs to the day it starts on
    fun isQuiet(quietHours: QuietHours, dayOfWeek: Int, minuteOfDay: Int): Boolean {
        if (!quietHours.enabled || quietHours.days.isEmpty()) return false
        val start = quietHours.startMinuteOfDay
        val end = quietHours.endMinuteOfDay
        if (start == end) return false
        return if (start < end) {
            dayOfWeek in quietHours.days && minuteOfDay in start until end
        } else {
            (dayOfWeek in quietHours.days && minuteOfDay >= start) ||
                (previousDay(dayOfWeek) in quietHours.days && minuteOfDay < end)
        }
    }

    private fun previousDay(day: Int) = if (day == 1) 7 else day - 1
}
