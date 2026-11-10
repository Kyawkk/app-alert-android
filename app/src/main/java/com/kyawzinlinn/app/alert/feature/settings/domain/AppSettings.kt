package com.kyawzinlinn.app.alert.feature.settings.domain

import com.kyawzinlinn.app.alert.core.common.ThemeMode

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
    val themeMode: ThemeMode = ThemeMode.System,
)
