package com.kyawzinlinn.app.alert.feature.settings.presentation

import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSection
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertScrollableContent
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertLoading
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCollapsingScreen
import com.kyawzinlinn.app.alert.core.designsystem.motion.entrance
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.permissions.PermissionState
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.presentation.components.AppearanceCard
import com.kyawzinlinn.app.alert.feature.settings.presentation.components.PermissionsSection
import com.kyawzinlinn.app.alert.feature.settings.presentation.components.QuietHoursCard
import com.kyawzinlinn.app.alert.feature.update.presentation.UpdateAction
import com.kyawzinlinn.app.alert.feature.update.presentation.UpdateUiState
import com.kyawzinlinn.app.alert.feature.update.presentation.components.UpdateSection

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onOpenAlarmSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onOpenDndAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    updateState: UpdateUiState,
    onUpdateAction: (UpdateAction) -> Unit,
    onInstallUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCollapsingScreen("Settings", modifier) {
        if (state.isLoading) {
            AppAlertLoading()
        } else {
            AppAlertScrollableContent {
                AppAlertSection("Appearance", Modifier.entrance(0)) {
                    AppearanceCard(
                        themeMode = state.themeMode,
                        onThemeModeChange = { onAction(SettingsAction.ThemeModeChanged(it)) },
                    )
                }
                AppAlertCard(Modifier.entrance(1)) {
                    AppAlertCardHeader("Alarm", description = "Sound, volume, duration, repeat and cooldown.")
                    AppAlertButton(
                        "Open alarm settings", onOpenAlarmSettings,
                        Modifier.fillMaxWidth().padding(top = Spacing.sm), AppAlertButtonStyle.Tonal,
                    )
                }
                AppAlertSection("Schedule", Modifier.entrance(2)) {
                    QuietHoursCard(state.quietHours, onAction)
                }
                PermissionsSection(
                    modifier = Modifier.entrance(3),
                    permissions = state.permissions,
                    onOpenNotificationAccess = onOpenNotificationAccess,
                    onRequestPostNotifications = onRequestPostNotifications,
                    onOpenDndAccess = onOpenDndAccess,
                    onOpenBatterySettings = onOpenBatterySettings,
                )
                UpdateSection(updateState, onUpdateAction, onInstallUpdate, Modifier.entrance(4))
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun SettingsScreenPreview() {
    AppAlertTheme {
        SettingsScreen(
            SettingsUiState(false, QuietHours(enabled = true), PermissionState(notificationAccessGranted = true)),
            {}, {}, {}, {}, {}, {}, UpdateUiState("1.0.0"), {}, {},
        )
    }
}
