package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.WindowWidth
import com.kyawzinlinn.app.alert.core.ui.rememberSoundPickerActions
import com.kyawzinlinn.app.alert.core.ui.rememberWindowWidth

@Composable
fun ApplicationsRoute(
    factory: AppViewModelFactory,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ApplicationsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onAction(ApplicationsAction.Refresh) }
    val isExpanded = rememberWindowWidth() == WindowWidth.Expanded
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }

    if (!isExpanded) {
        ApplicationsScreen(state, viewModel::onAction, onOpenDetails, modifier)
        return
    }

    Row(modifier.fillMaxSize()) {
        ApplicationsScreen(
            state = state,
            onAction = viewModel::onAction,
            onOpenDetails = { selectedPackage = it },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            highlightedPackage = selectedPackage,
        )
        val pkg = selectedPackage
        if (pkg == null) {
            AppAlertEmptyState(
                icon = Icons.Default.Info,
                title = "Select an app",
                message = "Choose \"Edit\" on a monitored app to configure it here.",
                modifier = Modifier.weight(1f).padding(Spacing.md),
            )
        } else {
            val detailViewModel: AppRuleDetailViewModel =
                viewModel(key = "detail-$pkg", factory = factory.forPackage(pkg))
            val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
            val pickers = rememberSoundPickerActions { detailViewModel.onAction(AppRuleDetailAction.SoundPicked(it)) }
            AppRuleDetailScreen(
                state = detailState,
                pickers = pickers,
                onAction = { action ->
                    detailViewModel.onAction(action)
                    if (action == AppRuleDetailAction.Remove) selectedPackage = null
                },
                onBack = null,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}
