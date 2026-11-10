package com.kyawzinlinn.app.alert.core

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kyawzinlinn.app.alert.core.common.ThemeMode
import com.kyawzinlinn.app.alert.core.datastore.PreferenceKeys
import com.kyawzinlinn.app.alert.feature.alarm.data.DataStoreAlarmSettingsRepository
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.settings.data.DataStoreSettingsRepository
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DataStoreRepositoriesTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var file: File

    private fun newStore() = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        file = File(context.cacheDir, "test_${System.nanoTime()}.preferences_pb")
    }

    @After fun tearDown() {
        scope.cancel()
        file.delete()
    }

    @Test fun defaultsAreReturnedWhenNothingIsStored() = runBlocking {
        val settings = DataStoreSettingsRepository(newStore()).observe().first()
        assertTrue(settings.monitoringEnabled)
        assertFalse(settings.onboardingCompleted)
        assertEquals(QuietHours(), settings.quietHours)
        assertEquals(AlarmSettings(), DataStoreAlarmSettingsRepository(newStore()).observe().first())
    }

    @Test fun themeModeIsPersistedAndUnknownValuesFallBackToSystem() = runBlocking {
        val store = newStore()
        val repository = DataStoreSettingsRepository(store)
        assertEquals(ThemeMode.System, repository.observe().first().themeMode)
        repository.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, DataStoreSettingsRepository(store).observe().first().themeMode)
        store.edit { it[PreferenceKeys.THEME_MODE] = "purple" }
        assertEquals(ThemeMode.System, repository.observe().first().themeMode)
    }

    @Test fun alarmSettingsAreSanitizedAndNullSoundClearsTheKey() = runBlocking {
        val repository = DataStoreAlarmSettingsRepository(newStore())
        repository.update { it.copy(volumePercent = 999, soundUri = "content://x", durationSeconds = 20) }
        val stored = repository.observe().first()
        assertEquals(100, stored.volumePercent)
        assertEquals("content://x", stored.soundUri)
        repository.update { it.copy(soundUri = null) }
        assertNull(repository.observe().first().soundUri)
    }

    @Test fun settingsSurviveRecreatingTheRepositoryOnTheSameFile() = runBlocking {
        val first = newStore()
        val settings = DataStoreSettingsRepository(first)
        settings.setMonitoringEnabled(false)
        settings.setQuietHours(QuietHours(true, 60, 120, setOf(1, 3)))
        settings.setOnboardingCompleted(true)
        scope.cancel() // closes the first store, like a process restart

        val restoredScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val restored = DataStoreSettingsRepository(
            PreferenceDataStoreFactory.create(scope = restoredScope, produceFile = { file }),
        ).observe().first()
        restoredScope.cancel()

        assertFalse(restored.monitoringEnabled)
        assertTrue(restored.onboardingCompleted)
        assertEquals(QuietHours(true, 60, 120, setOf(1, 3)), restored.quietHours)
    }
}
