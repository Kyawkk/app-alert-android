package com.kyawzinlinn.app.alert.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertScreenHeader
import com.kyawzinlinn.app.alert.core.designsystem.motion.entrance
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.ConstrainedWidth
import com.kyawzinlinn.app.alert.core.ui.WindowWidth
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.components.AlarmShortcutCard
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.components.MonitoringStatusCard
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.components.RecentAlertsSection
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.components.SelectedAppsSection

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    windowWidth: WindowWidth,
    onAction: (DashboardAction) -> Unit,
    onOpenApplications: () -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstrainedWidth(modifier) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            DashboardContent(
                state, windowWidth, onAction, onOpenApplications, onOpenApp,
                onOpenHistory, onOpenSettings, onOpenAlarmSettings, onOpenNotificationAccess,
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    windowWidth: WindowWidth,
    onAction: (DashboardAction) -> Unit,
    onOpenApplications: () -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
) {
    val statusCard: @Composable () -> Unit = {
        Box(Modifier.entrance(0)) {
            MonitoringStatusCard(
                status = state.monitoringStatus,
                isMonitoringEnabled = state.isMonitoringEnabled,
                selectedCount = state.selectedApps.size,
                onMonitoringChange = { onAction(DashboardAction.SetMonitoringEnabled(it)) },
                onOpenNotificationAccess = onOpenNotificationAccess,
                onChooseApps = onOpenApplications,
            )
        }
    }
    val appsSection: @Composable () -> Unit = {
        Box(Modifier.entrance(1)) { SelectedAppsSection(state.selectedApps, onOpenApplications, onOpenApp) }
    }
    val alarmCard: @Composable () -> Unit = {
        Box(Modifier.entrance(2)) {
            AlarmShortcutCard(
                isAlarmPlaying = state.isAlarmPlaying,
                failure = state.alarmFailure,
                onTestAlarm = { onAction(DashboardAction.TestAlarm) },
                onStopAlarm = { onAction(DashboardAction.StopAlarm) },
                onOpenAlarmSettings = onOpenAlarmSettings,
            )
        }
    }
    val alertsSection: @Composable () -> Unit = {
        Box(Modifier.entrance(3)) { RecentAlertsSection(state.recentAlerts, onOpenHistory) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        DashboardHeader(onOpenSettings)
        if (windowWidth == WindowWidth.Compact) {
            statusCard(); appsSection(); alarmCard(); alertsSection()
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    statusCard(); appsSection()
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    alarmCard(); alertsSection()
                }
            }
        }
        Box(Modifier.padding(bottom = Spacing.md))
    }
}

@Composable
private fun DashboardHeader(onOpenSettings: () -> Unit) {
    AppAlertScreenHeader("App Alert") {
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@AppAlertPreviews
@Composable
private fun DashboardScreenPreview() {
    AppAlertTheme {
        DashboardScreen(
            DashboardPreviewData.normal, WindowWidth.Compact, {}, {}, {}, {}, {}, {}, {},
        )
    }
}

@AppAlertPreviews
@Composable
private fun DashboardScreenEmptyPreview() {
    AppAlertTheme {
        DashboardScreen(DashboardPreviewData.empty, WindowWidth.Compact, {}, {}, {}, {}, {}, {}, {})
    }
}

@AppAlertPreviews
@Composable
private fun DashboardScreenLoadingPreview() {
    AppAlertTheme {
        DashboardScreen(DashboardUiState(isLoading = true), WindowWidth.Compact, {}, {}, {}, {}, {}, {}, {})
    }
}
