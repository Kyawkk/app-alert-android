package com.kyawzinlinn.app.alert.feature.history.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel

@Immutable
data class HistoryUiState(
    val isLoading: Boolean = true,
    val alerts: List<AlertUiModel> = emptyList(),
)
