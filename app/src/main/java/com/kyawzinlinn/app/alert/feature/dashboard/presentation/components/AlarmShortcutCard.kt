package com.kyawzinlinn.app.alert.feature.dashboard.presentation.components

import com.kyawzinlinn.app.alert.core.ui.AlarmTestButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertErrorText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
        AppAlertCardHeader(
            "Alarm",
            description = "Check that you can hear your alarm, or change its sound, volume and duration.",
        )
        if (failure != null) {
            AppAlertErrorText(failure.userMessage(), Modifier.padding(top = Spacing.xs))
        }
        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            AlarmTestButton(isAlarmPlaying, onTestAlarm, onStopAlarm)
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
