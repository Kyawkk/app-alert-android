package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome
import com.kyawzinlinn.app.alert.feature.settings.domain.AppSettings
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.testing.FakeAlarmPlayer
import com.kyawzinlinn.app.alert.testing.FakeAlarmSettingsRepository
import com.kyawzinlinn.app.alert.testing.FakeAppRulesRepository
import com.kyawzinlinn.app.alert.testing.FakeHistoryRepository
import com.kyawzinlinn.app.alert.testing.FakeSettingsRepository
import com.kyawzinlinn.app.alert.testing.FakeTimeProvider
import com.kyawzinlinn.app.alert.testing.event
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessIncomingNotificationUseCaseTest {
    private val time = FakeTimeProvider()
    private val rules = FakeAppRulesRepository(listOf(AppRule("com.viber.voip")))
    private val settings = FakeSettingsRepository()
    private val alarmSettings = FakeAlarmSettingsRepository(AlarmSettings(volumePercent = 55, cooldownSeconds = 30))
    private val player = FakeAlarmPlayer()
    private val history = FakeHistoryRepository()

    private val useCase = ProcessIncomingNotificationUseCase(
        appRules = rules,
        settings = settings,
        alarmSettings = alarmSettings,
        shouldTrigger = ShouldTriggerAlarmUseCase(AlertThrottle(), time),
        startAlarm = StartAlarmPlaybackUseCase(alarmSettings, player),
        history = history,
        timeProvider = time,
    )

    @Test fun `selected app plays the alarm with configured settings and records history`() = runTest {
        useCase(event())
        assertEquals(1, player.requests.size)
        assertEquals(55, player.requests.single().volumePercent)
        assertEquals(listOf("com.viber.voip" to AlertOutcome.TRIGGERED), history.records)
    }

    @Test fun `unselected app never reaches audio or history`() = runTest {
        useCase(event(pkg = "com.other.app"))
        assertTrue(player.requests.isEmpty())
        assertTrue(history.records.isEmpty())
    }

    @Test fun `global switch off plays nothing`() = runTest {
        settings.settings.value = AppSettings(monitoringEnabled = false)
        useCase(event())
        assertTrue(player.requests.isEmpty())
    }

    @Test fun `second message within cooldown is recorded as skipped`() = runTest {
        useCase(event(key = "a", text = "one"))
        useCase(event(key = "b", text = "two"))
        assertEquals(1, player.requests.size)
        assertEquals(AlertOutcome.SUPPRESSED_COOLDOWN, history.records.last().second)
    }

    @Test fun `duplicate callback is silent and not recorded`() = runTest {
        useCase(event())
        useCase(event())
        assertEquals(1, player.requests.size)
        assertEquals(1, history.records.size)
    }

    @Test fun `quiet hours skip is recorded`() = runTest {
        time.dayOfWeek = 3
        time.minuteOfDay = 23 * 60
        settings.settings.value = AppSettings(quietHours = QuietHours(enabled = true))
        useCase(event())
        assertTrue(player.requests.isEmpty())
        assertEquals(AlertOutcome.SUPPRESSED_QUIET_HOURS, history.records.single().second)
    }

    @Test fun `playback failure is recorded`() = runTest {
        player.result = AlarmStartResult.Failed(AlarmFailure.SOUND_UNREADABLE)
        useCase(event())
        assertEquals(AlertOutcome.PLAYBACK_FAILED, history.records.single().second)
    }

    @Test fun `repeat settings translate into the playback request`() = runTest {
        alarmSettings.settings.value = AlarmSettings(repeatEnabled = true, repeatCount = 3, repeatIntervalSeconds = 20, cooldownSeconds = 0)
        useCase(event())
        assertEquals(3, player.requests.single().repeatCount)
        assertEquals(20, player.requests.single().repeatIntervalSeconds)
    }

    @Test fun `failing history write does not break alarm handling`() = runTest {
        history.failOnRecord = true
        useCase(event())
        assertEquals(1, player.requests.size)
    }
}
