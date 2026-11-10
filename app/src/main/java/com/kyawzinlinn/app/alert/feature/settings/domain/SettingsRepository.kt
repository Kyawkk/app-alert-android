package com.kyawzinlinn.app.alert.feature.settings.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>

    suspend fun setMonitoringEnabled(enabled: Boolean)

    suspend fun setQuietHours(quietHours: QuietHours)

    /** Atomically reads, transforms and stores the quiet hours. */
    suspend fun updateQuietHours(transform: (QuietHours) -> QuietHours)

    suspend fun setOnboardingCompleted(completed: Boolean)
}
