package com.kyawzinlinn.app.alert.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertDialog
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.AlertListItem
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.core.ui.ConstrainedWidth
import com.kyawzinlinn.app.alert.core.ui.rememberCurrentTimeMillis
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onAction: (HistoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    ConstrainedWidth(modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "History",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                if (state.alerts.isNotEmpty()) {
                    AppAlertButton("Clear", { confirmClear = true }, style = AppAlertButtonStyle.Text)
                }
            }
            Text(
                "Kept for ${AlertHistoryRepository.RETENTION_DAYS} days, up to ${AlertHistoryRepository.MAX_ENTRIES} entries. " +
                    "Message contents are never stored.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.alerts.isEmpty() -> AppAlertEmptyState(
                    icon = Icons.Default.DateRange,
                    title = "No history yet",
                    message = "Alerts from your monitored apps will be listed here.",
                )
                else -> AppAlertCard(contentPadding = Spacing.xs, modifier = Modifier.weight(1f, fill = false)) {
                    val now by rememberCurrentTimeMillis()
                    LazyColumn(Modifier.padding(horizontal = Spacing.xs)) {
                        items(state.alerts, key = { it.id }, contentType = { "alert" }) {
                            AlertListItem(it, now, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AppAlertDialog(
            title = "Clear history?",
            text = "All recorded alerts will be removed. This can't be undone.",
            confirmText = "Clear",
            dismissText = "Cancel",
            onDismiss = { confirmClear = false },
            onConfirm = {
                onAction(HistoryAction.ClearHistory)
                confirmClear = false
            },
        )
    }
}

@AppAlertPreviews
@Composable
private fun HistoryScreenPreview() {
    AppAlertTheme {
        HistoryScreen(
            HistoryUiState(
                isLoading = false,
                alerts = listOf(
                    AlertUiModel(1, "com.viber.voip", "Viber", System.currentTimeMillis() - 60_000, AlertOutcome.TRIGGERED),
                    AlertUiModel(2, "com.viber.voip", "Viber", System.currentTimeMillis() - 7_200_000, AlertOutcome.SUPPRESSED_QUIET_HOURS),
                ),
            ),
            {},
        )
    }
}

@AppAlertPreviews
@Composable
private fun HistoryScreenEmptyPreview() {
    AppAlertTheme { HistoryScreen(HistoryUiState(isLoading = false), {}) }
}
