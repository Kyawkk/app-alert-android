package com.kyawzinlinn.app.alert.feature.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSectionHeader
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.preview.PreviewText
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.AppIcon
import com.kyawzinlinn.app.alert.core.ui.AppUiModel

@Composable
fun SelectedAppsSection(
    apps: List<AppUiModel>,
    onOpenApplications: () -> Unit,
    onOpenApp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        AppAlertSectionHeader(
            title = if (apps.isEmpty()) "Monitored apps" else "Monitored apps (${apps.size})",
            trailing = {
                AppAlertButton("Manage", onOpenApplications, style = AppAlertButtonStyle.Text)
            },
        )
        AppAlertCard {
            if (apps.isEmpty()) {
                AppAlertEmptyState(
                    icon = Icons.Default.Add,
                    title = "Nothing monitored yet",
                    message = "Pick the apps whose notifications should sound an alarm, for example Viber.",
                    action = { AppAlertButton("Choose apps", onOpenApplications) },
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    items(apps, key = { it.packageName }) { app ->
                        Column(
                            modifier = Modifier
                                .width(Dimens.appTileWidth)
                                .clickable { onOpenApp(app.packageName) }
                                .padding(vertical = Spacing.xxs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            AppIcon(app.packageName, app.label)
                            Text(
                                app.label,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun SelectedAppsSectionPreview() {
    AppAlertTheme {
        SelectedAppsSection(
            apps = listOf(
                AppUiModel("com.viber.voip", "Viber"),
                AppUiModel("a.b.c", PreviewText.LONG_APP_NAME),
            ),
            onOpenApplications = {}, onOpenApp = {}, modifier = Modifier.padding(Spacing.md),
        )
    }
}

@AppAlertPreviews
@Composable
private fun SelectedAppsSectionEmptyPreview() {
    AppAlertTheme {
        SelectedAppsSection(emptyList(), {}, {}, Modifier.padding(Spacing.md))
    }
}
