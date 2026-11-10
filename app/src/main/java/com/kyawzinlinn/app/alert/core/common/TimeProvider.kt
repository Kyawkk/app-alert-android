package com.kyawzinlinn.app.alert.core.common

import java.util.Calendar

/** Source of wall-clock information. Abstracted so time-dependent rules can be unit tested. */
interface TimeProvider {
    fun nowMillis(): Long

    /** ISO day of week: 1 = Monday ... 7 = Sunday, in the device time zone. */
    fun currentDayOfWeek(): Int

    fun currentMinuteOfDay(): Int
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun currentDayOfWeek(): Int {
        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sunday = 1
        return (calendarDay + 5) % 7 + 1
    }

    override fun currentMinuteOfDay(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
}
