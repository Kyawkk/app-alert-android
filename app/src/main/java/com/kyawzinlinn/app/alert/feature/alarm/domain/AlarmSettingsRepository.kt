package com.kyawzinlinn.app.alert.feature.alarm.domain

import kotlinx.coroutines.flow.Flow

interface AlarmSettingsRepository {
    fun observe(): Flow<AlarmSettings>

    suspend fun update(transform: (AlarmSettings) -> AlarmSettings)
}
