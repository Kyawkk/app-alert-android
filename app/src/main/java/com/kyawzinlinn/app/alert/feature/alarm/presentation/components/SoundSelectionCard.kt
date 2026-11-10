package com.kyawzinlinn.app.alert.feature.alarm.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.SoundPickerActions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundSelectionCard(
    title: String,
    soundTitle: String,
    currentUri: String?,
    pickers: SoundPickerActions,
    rejected: Boolean,
    modifier: Modifier = Modifier,
    onReset: (() -> Unit)? = null,
    resetText: String = "Use default",
) {
    AppAlertCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            soundTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.xxs),
        )
        if (rejected) {
            Text(
                "That sound can't be played, so it wasn't saved. Choose another one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Spacing.xxs),
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            AppAlertButton("System sounds", { pickers.openSystemPicker(currentUri) }, style = AppAlertButtonStyle.Tonal)
            AppAlertButton("Audio file", pickers.openFilePicker, style = AppAlertButtonStyle.Tonal)
            if (onReset != null) AppAlertButton(resetText, onReset, style = AppAlertButtonStyle.Text)
        }
    }
}

@AppAlertPreviews
@Composable
private fun SoundSelectionCardPreview() {
    AppAlertTheme {
        Column(Modifier.padding(Spacing.md)) {
            SoundSelectionCard(
                "Alarm sound", "Default alarm sound", null, SoundPickerActions({}, {}), rejected = false,
                onReset = {},
            )
        }
    }
}
