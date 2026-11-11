package com.kyawzinlinn.app.alert.feature.changelog.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.feature.update.data.UpdateConfig

@Composable
fun ChangelogRoute(
    factory: ViewModelProvider.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ChangelogViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    ChangelogScreen(
        versions = state.versions,
        installedVersion = state.installedVersion,
        selectedVersion = selected,
        onSelectVersion = { selected = it },
        onOpenRelease = { version ->
            val url = "https://github.com/${UpdateConfig.OWNER}/${UpdateConfig.REPO}/releases/tag/v${version.version}"
            // no browser installed: nothing useful to do
            runCatching { uriHandler.openUri(url) }
        },
        onBack = onBack,
        modifier = modifier,
    )
}
