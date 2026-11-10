package com.kyawzinlinn.app.alert.feature.dashboard.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.core.ui.AppUiModel
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.dashboard.domain.MonitoringStatus

@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val monitoringStatus: MonitoringStatus = MonitoringStatus.PAUSED_BY_USER,
    val isMonitoringEnabled: Boolean = false,
    val selectedApps: List<AppUiModel> = emptyList(),
    val recentAlerts: List<AlertUiModel> = emptyList(),
    val isAlarmPlaying: Boolean = false,
    val alarmFailure: AlarmFailure? = null,
)
