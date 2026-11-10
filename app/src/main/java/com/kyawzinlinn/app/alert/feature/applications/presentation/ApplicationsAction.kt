package com.kyawzinlinn.app.alert.feature.applications.presentation

sealed interface ApplicationsAction {
    data class QueryChanged(val query: String) : ApplicationsAction
    data class SetSelected(val packageName: String, val selected: Boolean) : ApplicationsAction
    data object Retry : ApplicationsAction

    /** Silent reload (no spinner), e.g. after returning from the Play Store. */
    data object Refresh : ApplicationsAction
}
