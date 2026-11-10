package com.kyawzinlinn.app.alert.feature.settings.domain

import com.kyawzinlinn.app.alert.core.common.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>

    suspend fun setMonitoringEnabled(enabled: Boolean)

    suspend fun setQuietHours(quietHours: QuietHours)

    suspend fun updateQuietHours(transform: (QuietHours) -> QuietHours)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setOnboardingCompleted(completed: Boolean)
}
