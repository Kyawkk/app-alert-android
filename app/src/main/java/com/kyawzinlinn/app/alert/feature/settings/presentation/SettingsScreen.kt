package com.kyawzinlinn.app.alert.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSectionHeader
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.permissions.PermissionState
import com.kyawzinlinn.app.alert.core.ui.ConstrainedWidth
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.presentation.components.PermissionsSection
import com.kyawzinlinn.app.alert.feature.settings.presentation.components.QuietHoursCard

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onOpenDndAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstrainedWidth(modifier) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@ConstrainedWidth
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = Spacing.sm).semantics { heading() },
            )
            AppAlertCard {
                Text("Alarm", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Sound, volume, duration, repeat and cooldown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppAlertButton(
                    "Open alarm settings", onOpenAlarmSettings,
                    Modifier.fillMaxWidth().padding(top = Spacing.sm), AppAlertButtonStyle.Tonal,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                AppAlertSectionHeader("Schedule")
                QuietHoursCard(state.quietHours, onAction)
            }
            PermissionsSection(
                permissions = state.permissions,
                onOpenNotificationAccess = onOpenNotificationAccess,
                onRequestPostNotifications = onRequestPostNotifications,
                onOpenDndAccess = onOpenDndAccess,
                onOpenBatterySettings = onOpenBatterySettings,
            )
            Box(Modifier.padding(bottom = Spacing.md))
        }
    }
}

@AppAlertPreviews
@Composable
private fun SettingsScreenPreview() {
    AppAlertTheme {
        SettingsScreen(
            SettingsUiState(false, QuietHours(enabled = true), PermissionState(notificationAccessGranted = true)),
            {}, {}, {}, {}, {}, {},
        )
    }
}
