package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertLoading
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
import com.kyawzinlinn.app.alert.core.designsystem.motion.entrance
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
        val mode = when {
            state.isLoading -> DetailMode.Loading
            !state.isSelected -> DetailMode.NotMonitored
            else -> DetailMode.Content
        }
        // loading, empty and content fade into each other instead of swapping abruptly
        AnimatedContent(
            targetState = mode,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { fadeIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS)) togetherWith fadeOut(tween(FADE_OUT_MILLIS)) },
            label = "ruleDetailMode",
        ) { shown ->
            when (shown) {
                DetailMode.Loading -> AppAlertLoading()
                DetailMode.NotMonitored -> AppAlertEmptyState(
                    icon = Icons.Default.Info,
                    title = "Not monitored",
                    message = "Turn this app on in the Applications list to configure it.",
                )
                DetailMode.Content -> DetailContent(state, pickers, onAction, onBack)
            }
        }
    }
}

private enum class DetailMode { Loading, NotMonitored, Content }

private const val FADE_IN_MILLIS = 200
private const val FADE_OUT_MILLIS = 90

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
        AppAlertCard(Modifier.entrance(0)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                AppIcon(state.packageName, state.label)
                Column(Modifier.weight(1f)) {
                    Text(state.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    AppAlertSupportingText(
                        state.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
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
            modifier = Modifier.entrance(1),
        )
        AppAlertCard(Modifier.entrance(2)) {
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
            keywords = state.keywords,
            scope = state.keywordScope,
            categories = state.categories,
            changed = state.filtersChanged,
            onAction = onAction,
            modifier = Modifier.entrance(3),
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
                soundTitle = "Uses the global alarm sound", keywords = listOf("urgent", "call"),
            ),
            SoundPickerActions({}, {}), {}, {},
        )
    }
}
