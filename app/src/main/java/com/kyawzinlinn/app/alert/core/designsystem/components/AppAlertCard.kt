package com.kyawzinlinn.app.alert.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing

/** Soft surface card used by every feature. Content size changes animate subtly. */
@Composable
fun AppAlertCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: Dp = Spacing.md,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(contentPadding),
            content = content,
        )
    }
}
