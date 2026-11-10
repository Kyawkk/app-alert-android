package com.kyawzinlinn.app.alert.feature.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HistoryRoute(factory: ViewModelProvider.Factory, modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state, viewModel::onAction, modifier)
}
