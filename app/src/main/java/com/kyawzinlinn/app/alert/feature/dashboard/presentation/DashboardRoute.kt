package com.kyawzinlinn.app.alert.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.core.permissions.SystemSettingsIntents
import com.kyawzinlinn.app.alert.core.permissions.startSettingsIntent
import com.kyawzinlinn.app.alert.core.ui.rememberWindowWidth

@Composable
fun DashboardRoute(
    factory: ViewModelProvider.Factory,
    onOpenApplications: () -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    DashboardScreen(
        state = state,
        windowWidth = rememberWindowWidth(),
        onAction = viewModel::onAction,
        onOpenApplications = onOpenApplications,
        onOpenApp = onOpenApp,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onOpenAlarmSettings = onOpenAlarmSettings,
        onOpenNotificationAccess = {
            context.startSettingsIntent(SystemSettingsIntents.notificationListenerSettings())
        },
        modifier = modifier,
    )
}
