package com.kyawzinlinn.app.alert.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.kyawzinlinn.app.alert.core.datastore.PreferenceKeys
import com.kyawzinlinn.app.alert.core.datastore.safeData
import com.kyawzinlinn.app.alert.feature.settings.domain.AppSettings
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsRepository {

    override fun observe(): Flow<AppSettings> = dataStore.safeData().map { prefs ->
        AppSettings(
            monitoringEnabled = prefs[PreferenceKeys.MONITORING_ENABLED] ?: true,
            onboardingCompleted = prefs[PreferenceKeys.ONBOARDING_COMPLETED] ?: false,
            quietHours = prefs.toQuietHours(),
        )
    }.distinctUntilChanged()

    override suspend fun setMonitoringEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.MONITORING_ENABLED] = enabled }
    }

    override suspend fun setQuietHours(quietHours: QuietHours) {
        dataStore.edit { it.write(quietHours) }
    }

    override suspend fun updateQuietHours(transform: (QuietHours) -> QuietHours) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toQuietHours())) }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETED] = completed }
    }

    private fun Preferences.toQuietHours(): QuietHours {
        val defaults = QuietHours()
        return QuietHours(
            enabled = this[PreferenceKeys.QUIET_ENABLED] ?: defaults.enabled,
            startMinuteOfDay = this[PreferenceKeys.QUIET_START] ?: defaults.startMinuteOfDay,
            endMinuteOfDay = this[PreferenceKeys.QUIET_END] ?: defaults.endMinuteOfDay,
            days = this[PreferenceKeys.QUIET_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: defaults.days,
        )
    }

    private fun MutablePreferences.write(quietHours: QuietHours) {
        this[PreferenceKeys.QUIET_ENABLED] = quietHours.enabled
        this[PreferenceKeys.QUIET_START] = quietHours.startMinuteOfDay
        this[PreferenceKeys.QUIET_END] = quietHours.endMinuteOfDay
        this[PreferenceKeys.QUIET_DAYS] = quietHours.days.map { it.toString() }.toSet()
    }
}
