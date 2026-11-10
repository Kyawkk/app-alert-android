package com.kyawzinlinn.app.alert.feature.applications.presentation

sealed interface ApplicationsAction {
    data class QueryChanged(val query: String) : ApplicationsAction
    data class SetSelected(val packageName: String, val selected: Boolean) : ApplicationsAction
    data object Retry : ApplicationsAction

    data object Refresh : ApplicationsAction
}
