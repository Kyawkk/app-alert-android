package com.kyawzinlinn.app.alert.feature.alarm.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.kyawzinlinn.app.alert.core.datastore.PreferenceKeys
import com.kyawzinlinn.app.alert.core.datastore.safeData
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreAlarmSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : AlarmSettingsRepository {

    override fun observe(): Flow<AlarmSettings> =
        dataStore.safeData().map { it.toAlarmSettings() }.distinctUntilChanged()

    override suspend fun update(transform: (AlarmSettings) -> AlarmSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toAlarmSettings())) }
    }

    private fun Preferences.toAlarmSettings(): AlarmSettings {
        val d = AlarmSettings()
        return AlarmSettings(
            soundUri = this[PreferenceKeys.SOUND_URI],
            volumePercent = this[PreferenceKeys.VOLUME] ?: d.volumePercent,
            durationSeconds = this[PreferenceKeys.DURATION] ?: d.durationSeconds,
            repeatEnabled = this[PreferenceKeys.REPEAT_ENABLED] ?: d.repeatEnabled,
            repeatCount = this[PreferenceKeys.REPEAT_COUNT] ?: d.repeatCount,
            repeatIntervalSeconds = this[PreferenceKeys.REPEAT_INTERVAL] ?: d.repeatIntervalSeconds,
            vibrationEnabled = this[PreferenceKeys.VIBRATION] ?: d.vibrationEnabled,
            cooldownSeconds = this[PreferenceKeys.COOLDOWN] ?: d.cooldownSeconds,
        ).sanitized()
    }

    private fun MutablePreferences.write(settings: AlarmSettings) {
        val uri = settings.soundUri
        if (uri == null) remove(PreferenceKeys.SOUND_URI) else this[PreferenceKeys.SOUND_URI] = uri
        this[PreferenceKeys.VOLUME] = settings.volumePercent
        this[PreferenceKeys.DURATION] = settings.durationSeconds
        this[PreferenceKeys.REPEAT_ENABLED] = settings.repeatEnabled
        this[PreferenceKeys.REPEAT_COUNT] = settings.repeatCount
        this[PreferenceKeys.REPEAT_INTERVAL] = settings.repeatIntervalSeconds
        this[PreferenceKeys.VIBRATION] = settings.vibrationEnabled
        this[PreferenceKeys.COOLDOWN] = settings.cooldownSeconds
    }
}
