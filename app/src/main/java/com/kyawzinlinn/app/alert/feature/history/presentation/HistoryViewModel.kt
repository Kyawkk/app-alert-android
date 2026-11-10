package com.kyawzinlinn.app.alert.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.core.ui.AlertUiModel
import com.kyawzinlinn.app.alert.feature.applications.domain.GetAppLabelUseCase
import com.kyawzinlinn.app.alert.feature.history.domain.ClearHistoryUseCase
import com.kyawzinlinn.app.alert.feature.history.domain.GetNotificationHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    getNotificationHistory: GetNotificationHistoryUseCase,
    private val getAppLabel: GetAppLabelUseCase,
    private val clearHistory: ClearHistoryUseCase,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getNotificationHistory().map { records ->
        HistoryUiState(
            isLoading = false,
            alerts = records.map {
                AlertUiModel(it.id, it.packageName, getAppLabel(it.packageName), it.timestampMillis, it.outcome)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onAction(action: HistoryAction) {
        when (action) {
            HistoryAction.ClearHistory -> viewModelScope.launchCatching { clearHistory() }
        }
    }
}
