package com.kyawzinlinn.app.alert.feature.changelog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogVersion
import com.kyawzinlinn.app.alert.feature.changelog.domain.GetChangelogUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChangelogUiState(
    val installedVersion: String = "",
    val versions: List<ChangelogVersion> = emptyList(),
)

class ChangelogViewModel(
    getChangelog: GetChangelogUseCase,
    installedVersion: String,
) : ViewModel() {
    val uiState: StateFlow<ChangelogUiState> = flow { emit(getChangelog()) }
        .map { ChangelogUiState(installedVersion, it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, ChangelogUiState(installedVersion))
}
