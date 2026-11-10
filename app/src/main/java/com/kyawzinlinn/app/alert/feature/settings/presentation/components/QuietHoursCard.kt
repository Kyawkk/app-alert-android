package com.kyawzinlinn.app.alert.feature.settings.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSwitchRow
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.formatMinuteOfDay
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.presentation.SettingsAction

private val DayLabels = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")

private enum class PickerTarget { Start, End }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuietHoursCard(
    quietHours: QuietHours,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var picker by rememberSaveable { mutableStateOf<PickerTarget?>(null) }

    AppAlertCard(modifier = modifier) {
        AppAlertSwitchRow(
            title = "Quiet hours",
            supportingText = "Stay silent during a daily window. Skipped alerts still appear in History.",
            checked = quietHours.enabled,
            onCheckedChange = { onAction(SettingsAction.QuietHoursEnabledChanged(it)) },
        )
        AnimatedVisibility(visible = quietHours.enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    AppAlertButton(
                        "From ${formatMinuteOfDay(quietHours.startMinuteOfDay)}",
                        { picker = PickerTarget.Start },
                        style = AppAlertButtonStyle.Tonal,
                    )
                    AppAlertButton(
                        "Until ${formatMinuteOfDay(quietHours.endMinuteOfDay)}",
                        { picker = PickerTarget.End },
                        style = AppAlertButtonStyle.Tonal,
                    )
                }
                if (quietHours.endMinuteOfDay <= quietHours.startMinuteOfDay) {
                    Text(
                        "This window continues past midnight into the next day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("Days the window starts", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = Spacing.xs))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    DayLabels.forEach { (day, label) ->
                        FilterChip(
                            selected = day in quietHours.days,
                            onClick = { onAction(SettingsAction.QuietDayToggled(day)) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    }

    when (picker) {
        PickerTarget.Start -> QuietTimePickerDialog(
            title = "Quiet hours start",
            initialMinuteOfDay = quietHours.startMinuteOfDay,
            onConfirm = { onAction(SettingsAction.QuietStartChanged(it)); picker = null },
            onDismiss = { picker = null },
        )
        PickerTarget.End -> QuietTimePickerDialog(
            title = "Quiet hours end",
            initialMinuteOfDay = quietHours.endMinuteOfDay,
            onConfirm = { onAction(SettingsAction.QuietEndChanged(it)); picker = null },
            onDismiss = { picker = null },
        )
        null -> Unit
    }
}

@AppAlertPreviews
@Composable
private fun QuietHoursCardPreview() {
    AppAlertTheme { QuietHoursCard(QuietHours(enabled = true), {}, Modifier.padding(Spacing.md)) }
}
