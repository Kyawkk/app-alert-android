package com.kyawzinlinn.app.alert.feature.settings.domain

/**
 * Silent window. Alarms are not played inside it (they are still logged to history).
 * Days are ISO numbers (1 = Monday ... 7 = Sunday) on which the window starts, which doubles as a
 * weekly monitoring schedule. If [endMinuteOfDay] <= [startMinuteOfDay] the window crosses midnight.
 */
data class QuietHours(
    val enabled: Boolean = false,
    val startMinuteOfDay: Int = 22 * 60,
    val endMinuteOfDay: Int = 7 * 60,
    val days: Set<Int> = ALL_DAYS,
) {
    companion object {
        val ALL_DAYS: Set<Int> = (1..7).toSet()
    }
}

data class AppSettings(
    val monitoringEnabled: Boolean = true,
    val quietHours: QuietHours = QuietHours(),
    val onboardingCompleted: Boolean = false,
)
