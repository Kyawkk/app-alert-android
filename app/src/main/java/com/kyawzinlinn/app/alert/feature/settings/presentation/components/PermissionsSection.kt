package com.kyawzinlinn.app.alert.feature.settings.presentation.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertPermissionCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSectionHeader
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.permissions.PermissionState

@Composable
fun PermissionsSection(
    permissions: PermissionState,
    onOpenNotificationAccess: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onOpenDndAccess: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        AppAlertSectionHeader("Permissions")
        AppAlertPermissionCard(
            title = "Notification access",
            description = "Lets App Alert see notifications from the apps you choose. Without it nothing can ring. " +
                "Android requires you to turn this on yourself.",
            granted = permissions.notificationAccessGranted,
            actionText = "Open notification access",
            onAction = onOpenNotificationAccess,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppAlertPermissionCard(
                title = "App Alert notifications",
                description = "Used only to show a \"Stop\" button while an alarm is ringing. Alarms still play without it.",
                granted = permissions.postNotificationsGranted,
                actionText = "Allow notifications",
                onAction = onRequestPostNotifications,
                optional = true,
            )
        }
        AppAlertPermissionCard(
            title = "Do Not Disturb access",
            description = "Optional. Android may silence alarms while Do Not Disturb is on. Granting access lets " +
                "you allow App Alert in Do Not Disturb settings. Behaviour varies by device; App Alert never changes it by itself.",
            granted = permissions.dndPolicyAccessGranted,
            actionText = "Open Do Not Disturb access",
            onAction = onOpenDndAccess,
            optional = true,
        )
        AppAlertPermissionCard(
            title = "Unrestricted battery use",
            description = "Optional. Some phones stop background apps aggressively. Exempting App Alert makes " +
                "monitoring more reliable with the screen off.",
            granted = permissions.ignoringBatteryOptimizations,
            actionText = "Open battery settings",
            onAction = onOpenBatterySettings,
            optional = true,
        )
    }
}

@AppAlertPreviews
@Composable
private fun PermissionsSectionPreview() {
    AppAlertTheme {
        PermissionsSection(
            PermissionState(notificationAccessGranted = true), {}, {}, {}, {},
            Modifier.padding(Spacing.md),
        )
    }
}
