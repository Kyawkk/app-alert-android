package com.kyawzinlinn.app.alert.feature.alarm.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTopBar
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.ConstrainedWidth
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
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
                    Box(Modifier.padding(bottom = Spacing.md))
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
