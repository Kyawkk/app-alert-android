package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.preview.PreviewText
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.AppIcon
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationItemUiModel

@Composable
fun ApplicationItem(
    app: ApplicationItemUiModel,
    onSelectedChange: (Boolean) -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    val background by animateColorAsState(
        targetValue = when {
            isHighlighted -> MaterialTheme.colorScheme.primaryContainer
            app.isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(180),
        label = "itemBackground",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .heightIn(min = Dimens.minTouchTarget)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .toggleable(
                    value = app.isSelected,
                    role = Role.Switch,
                    onValueChange = onSelectedChange,
                )
                .semantics { stateDescription = if (app.isSelected) "Monitored" else "Not monitored" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            AppIcon(app.packageName, app.label)
            Column(modifier = Modifier.weight(1f)) {
                Text(app.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (app.isSelected) "Monitored" else app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (app.isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(checked = app.isSelected, onCheckedChange = null)
        }
        if (app.isSelected) {
            androidx.compose.material3.TextButton(onClick = onOpenDetails) { Text("Edit") }
        }
    }
}

@AppAlertPreviews
@Composable
private fun ApplicationItemPreview() {
    AppAlertTheme {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            ApplicationItem(ApplicationItemUiModel("com.viber.voip", "Viber", true), {}, {})
            ApplicationItem(ApplicationItemUiModel("org.telegram.messenger", "Telegram", false), {}, {})
            ApplicationItem(ApplicationItemUiModel(PreviewText.PACKAGE, PreviewText.LONG_APP_NAME, false), {}, {})
        }
    }
}
