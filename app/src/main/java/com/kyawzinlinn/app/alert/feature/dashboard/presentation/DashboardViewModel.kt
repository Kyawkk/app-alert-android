package com.kyawzinlinn.app.alert.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.core.ui.AppUiModel
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmStartResult
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmPlaybackStateUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StartAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StopAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.GetAppLabelUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.ObserveSelectedApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.dashboard.domain.ObserveMonitoringStatusUseCase
import com.kyawzinlinn.app.alert.feature.history.domain.GetNotificationHistoryUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.ObserveAppSettingsUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.SetMonitoringEnabledUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    observeMonitoringStatus: ObserveMonitoringStatusUseCase,
    observeAppSettings: ObserveAppSettingsUseCase,
    observeSelectedApplications: ObserveSelectedApplicationsUseCase,
    getNotificationHistory: GetNotificationHistoryUseCase,
    observeAlarmPlaybackState: ObserveAlarmPlaybackStateUseCase,
    private val getAppLabel: GetAppLabelUseCase,
    private val setMonitoringEnabled: SetMonitoringEnabledUseCase,
    private val startAlarmPlayback: StartAlarmPlaybackUseCase,
    private val stopAlarmPlayback: StopAlarmPlaybackUseCase,
) : ViewModel() {

    private val alarmFailure = MutableStateFlow<AlarmFailure?>(null)

    // Every section is mapped and de-duplicated on its own. A new alert does not rebuild the app list,
    // and a theme change does not touch either, so Compose can skip the sections that did not change.
    private val selectedApps = observeSelectedApplications().map { rules ->
        val labels = getAppLabel.forAll(rules.map { it.packageName })
        rules
            .map { AppUiModel(it.packageName, labels.getValue(it.packageName)) }
            .sortedBy { it.label.lowercase() }
    }.distinctUntilChanged()

    private val recentAlerts = getNotificationHistory(RECENT_ALERT_LIMIT).map { history ->
        val labels = getAppLabel.forAll(history.map { it.packageName })
        history.map {
            AlertUiModel(it.id, it.packageName, labels.getValue(it.packageName), it.timestampMillis, it.outcome)
        }
    }.distinctUntilChanged()

    private val monitoringEnabled = observeAppSettings().map { it.monitoringEnabled }.distinctUntilChanged()

    private val content = combine(
        observeMonitoringStatus(),
        monitoringEnabled,
        selectedApps,
        recentAlerts,
        observeAlarmPlaybackState(),
    ) { status, enabled, apps, alerts, playing ->
        DashboardUiState(
            isLoading = false,
            monitoringStatus = status,
            isMonitoringEnabled = enabled,
            selectedApps = apps,
            recentAlerts = alerts,
            isAlarmPlaying = playing,
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(content, alarmFailure) { state, failure ->
        state.copy(alarmFailure = failure)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.SetMonitoringEnabled ->
                viewModelScope.launchCatching { setMonitoringEnabled(action.enabled) }
            DashboardAction.TestAlarm -> viewModelScope.launchCatching {
                alarmFailure.value = (startAlarmPlayback() as? AlarmStartResult.Failed)?.reason
            }
            DashboardAction.StopAlarm -> stopAlarmPlayback()
        }
    }

    private companion object {
        const val RECENT_ALERT_LIMIT = 5
    }
}
