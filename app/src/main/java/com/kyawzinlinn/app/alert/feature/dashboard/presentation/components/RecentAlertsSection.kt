package com.kyawzinlinn.app.alert.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSectionHeader
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.AlertListItem
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.core.ui.rememberCurrentTimeMillis
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome

@Composable
fun RecentAlertsSection(
    alerts: List<AlertUiModel>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        AppAlertSectionHeader(
            title = "Recent alerts",
            trailing = {
                if (alerts.isNotEmpty()) AppAlertButton("See all", onSeeAll, style = AppAlertButtonStyle.Text)
            },
        )
        AppAlertCard {
            if (alerts.isEmpty()) {
                AppAlertEmptyState(
                    icon = Icons.Default.Notifications,
                    title = "No alerts yet",
                    message = "When a monitored app notifies you, it will show up here.",
                )
            } else {
                val now by rememberCurrentTimeMillis()
                alerts.forEach { alert -> key(alert.id) { AlertListItem(alert, now) } }
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun RecentAlertsSectionPreview() {
    AppAlertTheme {
        RecentAlertsSection(
            listOf(
                AlertUiModel(1, "com.viber.voip", "Viber", System.currentTimeMillis() - 120_000, AlertOutcome.TRIGGERED),
                AlertUiModel(2, "com.viber.voip", "Viber", System.currentTimeMillis() - 3_600_000, AlertOutcome.SUPPRESSED_COOLDOWN),
            ),
            onSeeAll = {}, modifier = Modifier.padding(Spacing.md),
        )
    }
}

@AppAlertPreviews
@Composable
private fun RecentAlertsSectionEmptyPreview() {
    AppAlertTheme { RecentAlertsSection(emptyList(), {}, Modifier.padding(Spacing.md)) }
}
