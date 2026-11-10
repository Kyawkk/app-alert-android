package com.kyawzinlinn.app.alert.feature.alarm.presentation

import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertScrollableContent
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertLoading
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTopBar
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.components.ConstrainedWidth
import com.kyawzinlinn.app.alert.core.ui.SoundPickerActions
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.RepeatAndCooldownCard
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.SoundSelectionCard
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.TestAlarmCard
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.VolumeAndDurationCard

@Composable
fun AlarmScreen(
    state: AlarmUiState,
    pickers: SoundPickerActions,
    onAction: (AlarmAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        AppAlertTopBar(title = "Alarm", onBack = onBack)
        ConstrainedWidth {
            if (state.isLoading) {
                AppAlertLoading()
            } else {
                AppAlertScrollableContent {
                    SoundSelectionCard(
                        title = "Alarm sound",
                        soundTitle = state.soundTitle,
                        currentUri = state.settings.soundUri,
                        pickers = pickers,
                        rejected = state.soundRejected,
                        onReset = if (state.settings.soundUri != null) {
                            { onAction(AlarmAction.UseDefaultSound) }
                        } else null,
                    )
                    VolumeAndDurationCard(state.settings, onAction)
                    RepeatAndCooldownCard(state.settings, onAction)
                    TestAlarmCard(
                        isPlaying = state.isPlaying,
                        failure = state.playbackFailure,
                        onTest = { onAction(AlarmAction.TestAlarm) },
                        onStop = { onAction(AlarmAction.StopAlarm) },
                    )
                }
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun AlarmScreenPreview() {
    AppAlertTheme {
        AlarmScreen(
            AlarmUiState(isLoading = false, settings = AlarmSettings(repeatEnabled = true), soundTitle = "Default alarm sound"),
            SoundPickerActions({}, {}), {}, {},
        )
    }
}
