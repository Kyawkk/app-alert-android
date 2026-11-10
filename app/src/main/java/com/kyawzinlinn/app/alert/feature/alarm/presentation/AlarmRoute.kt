package com.kyawzinlinn.app.alert.feature.alarm.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.core.ui.rememberSoundPickerActions

@Composable
fun AlarmRoute(
    factory: ViewModelProvider.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AlarmViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickers = rememberSoundPickerActions { viewModel.onAction(AlarmAction.SoundPicked(it)) }
    AlarmScreen(state, pickers, viewModel::onAction, onBack, modifier)
}
