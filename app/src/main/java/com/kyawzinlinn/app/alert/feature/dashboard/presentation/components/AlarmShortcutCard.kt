package com.kyawzinlinn.app.alert.feature.dashboard.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.userMessage
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure

@Composable
fun AlarmShortcutCard(
    isAlarmPlaying: Boolean,
    failure: AlarmFailure?,
    onTestAlarm: () -> Unit,
    onStopAlarm: () -> Unit,
    onOpenAlarmSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        Text("Alarm", style = MaterialTheme.typography.titleMedium)
        Text(
            "Check that you can hear your alarm, or change its sound, volume and duration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xxs),
        )
        if (failure != null) {
            Text(
                failure.userMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            AnimatedContent(
                targetState = isAlarmPlaying,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
                label = "alarmShortcutButton",
            ) { playing ->
                if (playing) {
                    AppAlertButton("Stop alarm", onStopAlarm)
                } else {
                    AppAlertButton("Test alarm", onTestAlarm, style = AppAlertButtonStyle.Tonal)
                }
            }
            AppAlertButton("Alarm settings", onOpenAlarmSettings, style = AppAlertButtonStyle.Text)
        }
    }
}

@AppAlertPreviews
@Composable
private fun AlarmShortcutCardPreview() {
    AppAlertTheme { AlarmShortcutCard(false, null, {}, {}, {}, Modifier.padding(Spacing.md)) }
}

@AppAlertPreviews
@Composable
private fun AlarmShortcutCardPlayingPreview() {
    AppAlertTheme { AlarmShortcutCard(true, AlarmFailure.SOUND_UNREADABLE, {}, {}, {}, Modifier.padding(Spacing.md)) }
}
