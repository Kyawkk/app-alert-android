package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.animation.Crossfade
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertLoading
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCollapsingScreen
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.applications.presentation.components.ApplicationSearchField
import com.kyawzinlinn.app.alert.feature.applications.presentation.components.ApplicationsList

private enum class ListMode { Loading, Failed, Content }

@Composable
fun ApplicationsScreen(
    state: ApplicationsUiState,
    onAction: (ApplicationsAction) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    highlightedPackage: String? = null,
) {
    if (showTitle) {
        AppAlertCollapsingScreen("Applications", modifier) {
            ApplicationsBody(state, onAction, onOpenDetails, Modifier, highlightedPackage)
        }
    } else {
        ApplicationsBody(state, onAction, onOpenDetails, modifier, highlightedPackage)
    }
}

@Composable
private fun ApplicationsBody(
    state: ApplicationsUiState,
    onAction: (ApplicationsAction) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier,
    highlightedPackage: String?,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AppAlertSupportingText(
            if (state.isLoading || state.loadFailed) "Choose apps to monitor"
            else "${state.selectedCount} of ${state.totalCount} apps monitored",
        )
        ApplicationSearchField(state.query, { onAction(ApplicationsAction.QueryChanged(it)) })
        val mode = when {
            state.isLoading -> ListMode.Loading
            state.loadFailed -> ListMode.Failed
            else -> ListMode.Content
        }
        Crossfade(targetState = mode, animationSpec = tween(200), label = "applicationsMode") { shown ->
            when (shown) {
                ListMode.Loading -> AppAlertLoading()
                ListMode.Failed -> AppAlertEmptyState(
                    icon = Icons.Default.Warning,
                    title = "Couldn't load your apps",
                    message = "Android did not return the list of installed apps.",
                    action = { AppAlertButton("Try again", { onAction(ApplicationsAction.Retry) }) },
                )
                ListMode.Content -> ApplicationsList(
                    apps = state.apps,
                    query = state.query,
                    onSelectedChange = { pkg, selected -> onAction(ApplicationsAction.SetSelected(pkg, selected)) },
                    onOpenDetails = onOpenDetails,
                    highlightedPackage = highlightedPackage,
                )
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun ApplicationsScreenPreview() {
    AppAlertTheme { ApplicationsScreen(ApplicationsPreviewData.normal, {}, {}) }
}

@AppAlertPreviews
@Composable
private fun ApplicationsScreenEmptySearchPreview() {
    AppAlertTheme { ApplicationsScreen(ApplicationsPreviewData.noResults, {}, {}) }
}

@AppAlertPreviews
@Composable
private fun ApplicationsScreenLoadingPreview() {
    AppAlertTheme { ApplicationsScreen(ApplicationsUiState(isLoading = true), {}, {}) }
}
