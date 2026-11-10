package com.kyawzinlinn.app.alert.feature.settings.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.core.permissions.SystemSettingsIntents
import com.kyawzinlinn.app.alert.core.permissions.startSettingsIntent

@Composable
fun SettingsRoute(
    factory: ViewModelProvider.Factory,
    onOpenAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val postNotificationsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onAction(SettingsAction.RefreshPermissions)
        if (!granted) context.startSettingsIntent(SystemSettingsIntents.appNotificationSettings(context))
    }
    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenAlarmSettings = onOpenAlarmSettings,
        onOpenNotificationAccess = {
            context.startSettingsIntent(SystemSettingsIntents.notificationListenerSettings())
        },
        onRequestPostNotifications = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onOpenDndAccess = {
            context.startSettingsIntent(
                SystemSettingsIntents.notificationPolicySettings(), SystemSettingsIntents.appDetails(context),
            )
        },
        onOpenBatterySettings = {
            context.startSettingsIntent(
                SystemSettingsIntents.batteryOptimizationSettings(), SystemSettingsIntents.appDetails(context),
            )
        },
        modifier = modifier,
    )
}
