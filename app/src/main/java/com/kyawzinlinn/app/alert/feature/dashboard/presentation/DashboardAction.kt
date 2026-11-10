package com.kyawzinlinn.app.alert.feature.dashboard.presentation

sealed interface DashboardAction {
    data class SetMonitoringEnabled(val enabled: Boolean) : DashboardAction
    data object TestAlarm : DashboardAction
    data object StopAlarm : DashboardAction
}
