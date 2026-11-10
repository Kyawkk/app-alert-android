package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.core.ui.rememberSoundPickerActions

@Composable
fun AppRuleDetailRoute(
    packageName: String,
    factory: AppViewModelFactory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AppRuleDetailViewModel =
        viewModel(key = "detail-$packageName", factory = factory.forPackage(packageName))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickers = rememberSoundPickerActions { viewModel.onAction(AppRuleDetailAction.SoundPicked(it)) }
    AppRuleDetailScreen(state, pickers, viewModel::onAction, onBack, modifier)
}
