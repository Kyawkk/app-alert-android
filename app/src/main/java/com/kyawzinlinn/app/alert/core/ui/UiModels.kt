package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome

@Immutable
data class AppUiModel(val packageName: String, val label: String)

@Immutable
data class AlertUiModel(
    val id: Long,
    val packageName: String,
    val appLabel: String,
    val timestampMillis: Long,
    val outcome: AlertOutcome,
)
