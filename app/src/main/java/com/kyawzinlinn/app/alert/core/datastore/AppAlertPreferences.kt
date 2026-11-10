package com.kyawzinlinn.app.alert.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

val Context.appAlertDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_alert_preferences")

object PreferenceKeys {
    val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val QUIET_ENABLED = booleanPreferencesKey("quiet_enabled")
    val QUIET_START = intPreferencesKey("quiet_start_minute")
    val QUIET_END = intPreferencesKey("quiet_end_minute")
    val QUIET_DAYS = stringSetPreferencesKey("quiet_days")

    val SOUND_URI = stringPreferencesKey("sound_uri")
    val VOLUME = intPreferencesKey("volume_percent")
    val DURATION = intPreferencesKey("duration_seconds")
    val REPEAT_ENABLED = booleanPreferencesKey("repeat_enabled")
    val REPEAT_COUNT = intPreferencesKey("repeat_count")
    val REPEAT_INTERVAL = intPreferencesKey("repeat_interval_seconds")
    val VIBRATION = booleanPreferencesKey("vibration_enabled")
    val COOLDOWN = intPreferencesKey("cooldown_seconds")
}

fun DataStore<Preferences>.safeData(): Flow<Preferences> =
    data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
