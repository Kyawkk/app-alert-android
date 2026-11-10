package com.kyawzinlinn.app.alert.feature.settings.presentation

import com.kyawzinlinn.app.alert.core.common.ThemeMode

sealed interface SettingsAction {
    data class QuietHoursEnabledChanged(val enabled: Boolean) : SettingsAction
    data class QuietStartChanged(val minuteOfDay: Int) : SettingsAction
    data class QuietEndChanged(val minuteOfDay: Int) : SettingsAction
    data class QuietDayToggled(val isoDay: Int) : SettingsAction
    data class ThemeModeChanged(val mode: ThemeMode) : SettingsAction
    data object RefreshPermissions : SettingsAction
}
