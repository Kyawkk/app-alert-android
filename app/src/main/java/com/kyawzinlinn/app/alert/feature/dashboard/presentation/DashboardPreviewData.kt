package com.kyawzinlinn.app.alert.feature.dashboard.presentation

import com.kyawzinlinn.app.alert.core.designsystem.preview.PreviewText
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.core.ui.AppUiModel
import com.kyawzinlinn.app.alert.feature.dashboard.domain.MonitoringStatus
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome

object DashboardPreviewData {
    val normal = DashboardUiState(
        isLoading = false,
        monitoringStatus = MonitoringStatus.ACTIVE,
        isMonitoringEnabled = true,
        selectedApps = listOf(
            AppUiModel("com.viber.voip", "Viber"),
            AppUiModel("org.telegram.messenger", "Telegram"),
            AppUiModel("long.name.app", PreviewText.LONG_APP_NAME),
        ),
        recentAlerts = listOf(
            AlertUiModel(1, "com.viber.voip", "Viber", 1_700_000_000_000, AlertOutcome.TRIGGERED),
            AlertUiModel(2, "org.telegram.messenger", "Telegram", 1_700_000_100_000, AlertOutcome.SUPPRESSED_COOLDOWN),
        ),
    )

    val empty = DashboardUiState(
        isLoading = false,
        monitoringStatus = MonitoringStatus.NO_APPS_SELECTED,
        isMonitoringEnabled = true,
    )
}
