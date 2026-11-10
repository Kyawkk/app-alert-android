package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSwitchRow
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.AppIcon
import com.kyawzinlinn.app.alert.core.ui.SoundPickerActions
import com.kyawzinlinn.app.alert.core.ui.userMessage
import com.kyawzinlinn.app.alert.feature.alarm.presentation.components.SoundSelectionCard
import com.kyawzinlinn.app.alert.feature.applications.presentation.components.NotificationFiltersCard

@Composable
fun AppRuleDetailScreen(
    state: AppRuleDetailUiState,
    pickers: SoundPickerActions,
    onAction: (AppRuleDetailAction) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTopBar(
            title = state.label.ifEmpty { "App settings" },
            onBack = onBack,
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !state.isSelected -> AppAlertEmptyState(
                icon = Icons.Default.Info,
                title = "Not monitored",
                message = "Turn this app on in the Applications list to configure it.",
            )
            else -> DetailContent(state, pickers, onAction, onBack)
        }
    }
}

@Composable
private fun DetailContent(
    state: AppRuleDetailUiState,
    pickers: SoundPickerActions,
    onAction: (AppRuleDetailAction) -> Unit,
    onBack: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        AppAlertCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AppIcon(state.packageName, state.label)
                Column(Modifier.weight(1f)) {
                    Text(state.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        state.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AppAlertSwitchRow(
                title = "Alert for this app",
                supportingText = "Turn off to pause this app without removing its settings",
                checked = state.enabled,
                onCheckedChange = { onAction(AppRuleDetailAction.SetEnabled(it)) },
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
        SoundSelectionCard(
            title = "Sound for this app",
            soundTitle = state.soundTitle,
            currentUri = state.soundUri,
            pickers = pickers,
            rejected = state.soundRejected,
            onReset = if (state.soundUri != null) {
                { onAction(AppRuleDetailAction.UseGlobalSound) }
            } else null,
            resetText = "Use global sound",
        )
        AppAlertCard {
            state.playbackFailure?.let {
                Text(it.userMessage(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (state.isTestPlaying) {
                AppAlertButton("Stop", { onAction(AppRuleDetailAction.StopSound) }, Modifier.fillMaxWidth())
            } else {
                AppAlertButton(
                    "Test this app's sound", { onAction(AppRuleDetailAction.TestSound) },
                    Modifier.fillMaxWidth(), AppAlertButtonStyle.Tonal,
                )
            }
        }
        NotificationFiltersCard(
            keywords = state.keywordsInput,
            scope = state.keywordScope,
            categories = state.categories,
            changed = state.filtersChanged,
            onAction = onAction,
        )
        AppAlertButton(
            "Stop monitoring this app",
            {
                onAction(AppRuleDetailAction.Remove)
                onBack?.invoke()
            },
            Modifier.fillMaxWidth(),
            AppAlertButtonStyle.Outlined,
        )
        Box(Modifier.padding(bottom = Spacing.md))
    }
}

@AppAlertPreviews
@Composable
private fun AppRuleDetailScreenPreview() {
    AppAlertTheme {
        AppRuleDetailScreen(
            AppRuleDetailUiState(
                isLoading = false, isSelected = true, packageName = "com.viber.voip", label = "Viber",
                soundTitle = "Uses the global alarm sound", keywordsInput = "urgent",
            ),
            SoundPickerActions({}, {}), {}, {},
        )
    }
}
