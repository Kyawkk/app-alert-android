package com.kyawzinlinn.app.alert.feature.dashboard.domain

import com.kyawzinlinn.app.alert.core.notifications.ListenerConnection
import com.kyawzinlinn.app.alert.core.permissions.PermissionState
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.settings.domain.AppSettings
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.testing.FakeAppRulesRepository
import com.kyawzinlinn.app.alert.testing.FakePermissionStatusSource
import com.kyawzinlinn.app.alert.testing.FakeSettingsRepository
import com.kyawzinlinn.app.alert.testing.FakeTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveMonitoringStatusUseCaseTest {
    private val settings = FakeSettingsRepository()
    private val rules = FakeAppRulesRepository(listOf(AppRule("com.viber.voip")))
    private val permissions = FakePermissionStatusSource(PermissionState(notificationAccessGranted = true))
    private val connection = ListenerConnection().apply { setConnected(true) }
    private val time = FakeTimeProvider()

    private suspend fun status() = ObserveMonitoringStatusUseCase(
        settings, rules, permissions, connection, time, tickMillis = Long.MAX_VALUE,
    )().first()

    @Test fun `active only when everything is in place`() = runTest {
        assertEquals(MonitoringStatus.ACTIVE, status())
    }

    @Test fun `user pause wins`() = runTest {
        settings.settings.value = AppSettings(monitoringEnabled = false)
        assertEquals(MonitoringStatus.PAUSED_BY_USER, status())
    }

    @Test fun `missing notification access is never reported as active`() = runTest {
        permissions.set(PermissionState(notificationAccessGranted = false))
        assertEquals(MonitoringStatus.NOTIFICATION_ACCESS_MISSING, status())
    }

    @Test fun `granted but unbound listener is reported as disconnected`() = runTest {
        connection.setConnected(false)
        assertEquals(MonitoringStatus.LISTENER_DISCONNECTED, status())
    }

    @Test fun `no enabled apps`() = runTest {
        rules.remove("com.viber.voip")
        assertEquals(MonitoringStatus.NO_APPS_SELECTED, status())
    }

    @Test fun `quiet hours are reported`() = runTest {
        time.dayOfWeek = 2
        time.minuteOfDay = 23 * 60
        settings.settings.value = AppSettings(quietHours = QuietHours(enabled = true))
        assertEquals(MonitoringStatus.IN_QUIET_HOURS, status())
    }
}
