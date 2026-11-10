@file:OptIn(ExperimentalMaterial3Api::class)

package com.kyawzinlinn.app.alert.feature.settings.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertDialog

@Composable
fun QuietTimePickerDialog(
    title: String,
    initialMinuteOfDay: Int,
    onConfirm: (minuteOfDay: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialMinuteOfDay / 60,
        initialMinute = initialMinuteOfDay % 60,
        is24Hour = true,
    )
    AppAlertDialog(
        title = title,
        onDismiss = onDismiss,
        confirmText = "OK",
        dismissText = "Cancel",
        onConfirm = { onConfirm(state.hour * 60 + state.minute) },
        content = { TimePicker(state = state) },
    )
}
