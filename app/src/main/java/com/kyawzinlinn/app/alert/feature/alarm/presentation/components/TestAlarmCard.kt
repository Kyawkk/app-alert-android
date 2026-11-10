package com.kyawzinlinn.app.alert.feature.alarm.presentation.components

import com.kyawzinlinn.app.alert.core.ui.AlarmTestButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertErrorText
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.userMessage
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure

@Composable
fun TestAlarmCard(
    isPlaying: Boolean,
    failure: AlarmFailure?,
    onTest: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        AppAlertCardHeader("Test", description = "Plays the alarm exactly as it will sound for a real notification.")
        if (failure != null) {
            AppAlertErrorText(failure.userMessage(), Modifier.padding(top = Spacing.xxs))
        }
        AlarmTestButton(
            isPlaying = isPlaying,
            onTest = onTest,
            onStop = onStop,
            modifier = Modifier.padding(top = Spacing.sm),
            fillWidth = true,
        )
    }
}
