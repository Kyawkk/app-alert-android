package com.kyawzinlinn.app.alert.feature.alarm.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSlider
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSwitchRow
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.formatSeconds
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.presentation.AlarmAction

@Composable
fun VolumeAndDurationCard(
    settings: AlarmSettings,
    onAction: (AlarmAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        AppAlertSlider(
            label = "Volume",
            value = settings.volumePercent,
            valueRange = AlarmSettings.VOLUME_RANGE,
            valueText = { "$it%" },
            onValueChangeFinished = { onAction(AlarmAction.VolumeChanged(it)) },
        )
        Text(
            "Relative to your phone's alarm volume. App Alert never changes the system volume.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppAlertSlider(
            label = "Duration",
            value = settings.durationSeconds,
            valueRange = AlarmSettings.DURATION_RANGE,
            valueText = ::formatSeconds,
            onValueChangeFinished = { onAction(AlarmAction.DurationChanged(it)) },
            modifier = Modifier.padding(top = Spacing.sm),
        )
        AppAlertSwitchRow(
            title = "Vibrate",
            checked = settings.vibrationEnabled,
            onCheckedChange = { onAction(AlarmAction.VibrationChanged(it)) },
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}

@Composable
fun RepeatAndCooldownCard(
    settings: AlarmSettings,
    onAction: (AlarmAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        AppAlertSwitchRow(
            title = "Repeat alarm",
            supportingText = "Ring again if you haven't reacted",
            checked = settings.repeatEnabled,
            onCheckedChange = { onAction(AlarmAction.RepeatEnabledChanged(it)) },
        )
        AnimatedVisibility(visible = settings.repeatEnabled) {
            Column {
                AppAlertSlider(
                    label = "Repeat times",
                    value = settings.repeatCount,
                    valueRange = AlarmSettings.REPEAT_COUNT_RANGE,
                    valueText = { "$it×" },
                    onValueChangeFinished = { onAction(AlarmAction.RepeatCountChanged(it)) },
                )
                AppAlertSlider(
                    label = "Pause between repeats",
                    value = settings.repeatIntervalSeconds,
                    valueRange = AlarmSettings.REPEAT_INTERVAL_RANGE,
                    valueText = ::formatSeconds,
                    onValueChangeFinished = { onAction(AlarmAction.RepeatIntervalChanged(it)) },
                )
            }
        }
        AppAlertSlider(
            label = "Cooldown per app",
            value = settings.cooldownSeconds,
            valueRange = AlarmSettings.COOLDOWN_RANGE,
            valueText = { if (it == 0) "Off" else formatSeconds(it) },
            onValueChangeFinished = { onAction(AlarmAction.CooldownChanged(it)) },
            modifier = Modifier.padding(top = Spacing.sm),
        )
        Text(
            "After an alarm, further notifications from the same app stay silent for this long.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@AppAlertPreviews
@Composable
private fun AlarmSettingsCardsPreview() {
    AppAlertTheme {
        Column(Modifier.padding(Spacing.md)) {
            VolumeAndDurationCard(AlarmSettings(), {})
            RepeatAndCooldownCard(AlarmSettings(repeatEnabled = true), {}, Modifier.padding(top = Spacing.md))
        }
    }
}
