package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing

/** One history/alert row: app icon, name, outcome (as text, not only color) and relative time. */
@Composable
fun AlertListItem(
    alert: AlertUiModel,
    nowMillis: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.minTouchTarget)
            .padding(vertical = Spacing.xs)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        AppIcon(alert.packageName, alert.appLabel, size = Dimens.smallAppIconSize)
        Column(modifier = Modifier.weight(1f)) {
            Text(alert.appLabel, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                alert.outcome.label(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            formatRelativeTime(alert.timestampMillis, nowMillis),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
