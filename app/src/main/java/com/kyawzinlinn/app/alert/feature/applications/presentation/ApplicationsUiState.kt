package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class ApplicationItemUiModel(
    val packageName: String,
    val label: String,
    val isSelected: Boolean,
)

@Immutable
data class ApplicationsUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val query: String = "",
    val apps: List<ApplicationItemUiModel> = emptyList(),
    val selectedCount: Int = 0,
    val totalCount: Int = 0,
)
