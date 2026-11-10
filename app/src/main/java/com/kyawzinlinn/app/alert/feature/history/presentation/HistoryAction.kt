package com.kyawzinlinn.app.alert.feature.history.presentation

sealed interface HistoryAction {
    data object ClearHistory : HistoryAction
}
