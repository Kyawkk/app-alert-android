package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationItemUiModel

@Composable
fun ApplicationsList(
    apps: List<ApplicationItemUiModel>,
    query: String,
    onSelectedChange: (String, Boolean) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
    highlightedPackage: String? = null,
) {
    if (apps.isEmpty()) {
        AppAlertEmptyState(
            icon = Icons.Default.Search,
            title = if (query.isBlank()) "No apps found" else "No matching apps",
            message = if (query.isBlank()) "No launchable apps were found on this device."
            else "Nothing matches \"${query.trim()}\". Check the spelling or try a shorter name.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        items(apps, key = { it.packageName }, contentType = { "app" }) { app ->
            ApplicationItem(
                app = app,
                onSelectedChange = { onSelectedChange(app.packageName, it) },
                onOpenDetails = { onOpenDetails(app.packageName) },
                isHighlighted = app.packageName == highlightedPackage,
                modifier = Modifier.animateItem(),
            )
        }
    }
}
