package com.kyawzinlinn.app.alert.feature.alarm.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
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
        Text("Test", style = MaterialTheme.typography.titleMedium)
        Text(
            "Plays the alarm exactly as it will sound for a real notification.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (failure != null) {
            Text(
                failure.userMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        }
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
            label = "testAlarmButton",
        ) { playing ->
            if (playing) {
                AppAlertButton("Stop alarm", onStop, Modifier.fillMaxWidth().padding(top = Spacing.sm))
            } else {
                AppAlertButton(
                    "Test alarm", onTest, Modifier.fillMaxWidth().padding(top = Spacing.sm), AppAlertButtonStyle.Tonal,
                )
            }
        }
    }
}
