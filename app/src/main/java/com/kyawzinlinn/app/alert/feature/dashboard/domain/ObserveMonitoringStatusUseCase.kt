package com.kyawzinlinn.app.alert.feature.dashboard.domain

import com.kyawzinlinn.app.alert.core.common.TimeProvider
import com.kyawzinlinn.app.alert.core.notifications.ListenerConnection
import com.kyawzinlinn.app.alert.core.permissions.PermissionStatusSource
import com.kyawzinlinn.app.alert.feature.alarm.domain.QuietHoursEvaluator
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRulesRepository
import com.kyawzinlinn.app.alert.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/**
 * Never reports "active" unless notification access is granted AND the listener is bound.
 * A once-a-minute tick (only while collected) lets quiet-hours boundaries update the status.
 */
class ObserveMonitoringStatusUseCase(
    private val settings: SettingsRepository,
    private val rules: AppRulesRepository,
    private val permissions: PermissionStatusSource,
    private val listenerConnection: ListenerConnection,
    private val timeProvider: TimeProvider,
    private val tickMillis: Long = 60_000L,
) {
    operator fun invoke(): Flow<MonitoringStatus> = combine(
        settings.observe(),
        rules.observeRules(),
        permissions.state,
        listenerConnection.isConnected,
        ticker(),
    ) { appSettings, appRules, permissionState, connected, _ ->
        when {
            !appSettings.monitoringEnabled -> MonitoringStatus.PAUSED_BY_USER
            !permissionState.notificationAccessGranted -> MonitoringStatus.NOTIFICATION_ACCESS_MISSING
            !connected -> MonitoringStatus.LISTENER_DISCONNECTED
            appRules.none { it.enabled } -> MonitoringStatus.NO_APPS_SELECTED
            QuietHoursEvaluator.isQuiet(
                appSettings.quietHours, timeProvider.currentDayOfWeek(), timeProvider.currentMinuteOfDay(),
            ) -> MonitoringStatus.IN_QUIET_HOURS
            else -> MonitoringStatus.ACTIVE
        }
    }.distinctUntilChanged()

    private fun ticker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(tickMillis)
        }
    }
}
