package com.kyawzinlinn.app.alert.feature.onboarding.presentation

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.permissions.SystemSettingsIntents
import com.kyawzinlinn.app.alert.core.permissions.startSettingsIntent
import com.kyawzinlinn.app.alert.core.ui.rememberSoundPickerActions
import com.kyawzinlinn.app.alert.feature.alarm.presentation.AlarmAction
import com.kyawzinlinn.app.alert.feature.alarm.presentation.AlarmViewModel
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.SoundSelectionCard
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.TestAlarmCard
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsScreen
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsViewModel

@Composable
fun OnboardingRoute(
    factory: AppViewModelFactory,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OnboardingViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationsViewModel: ApplicationsViewModel = viewModel(factory = factory)
    val alarmViewModel: AlarmViewModel = viewModel(factory = factory)
    val context = LocalContext.current

    OnboardingScreen(
        state = state,
        onAction = { viewModel.onAction(it, onFinished) },
        onOpenNotificationAccess = {
            context.startSettingsIntent(SystemSettingsIntents.notificationListenerSettings())
        },
        onOpenDndAccess = {
            context.startSettingsIntent(
                SystemSettingsIntents.notificationPolicySettings(), SystemSettingsIntents.appDetails(context),
            )
        },
        firstAppContent = {
            val appsState by applicationsViewModel.uiState.collectAsStateWithLifecycle()
            Column(Modifier.heightIn(max = 480.dp)) {
                ApplicationsScreen(
                    state = appsState,
                    onAction = applicationsViewModel::onAction,
                    onOpenDetails = {},
                    showTitle = false,
                )
            }
        },
        soundContent = {
            val alarmState by alarmViewModel.uiState.collectAsStateWithLifecycle()
            val pickers = rememberSoundPickerActions { alarmViewModel.onAction(AlarmAction.SoundPicked(it)) }
            SoundSelectionCard(
                title = "Alarm sound",
                soundTitle = alarmState.soundTitle,
                currentUri = alarmState.settings.soundUri,
                pickers = pickers,
                rejected = alarmState.soundRejected,
                onReset = if (alarmState.settings.soundUri != null) {
                    { alarmViewModel.onAction(AlarmAction.UseDefaultSound) }
                } else null,
            )
        },
        testContent = {
            val alarmState by alarmViewModel.uiState.collectAsStateWithLifecycle()
            TestAlarmCard(
                isPlaying = alarmState.isPlaying,
                failure = alarmState.playbackFailure,
                onTest = { alarmViewModel.onAction(AlarmAction.TestAlarm) },
                onStop = { alarmViewModel.onAction(AlarmAction.StopAlarm) },
                modifier = Modifier.padding(bottom = Spacing.md),
            )
        },
        modifier = modifier,
    )
}
