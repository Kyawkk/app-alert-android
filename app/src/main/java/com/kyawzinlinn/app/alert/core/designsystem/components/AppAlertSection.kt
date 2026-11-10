package com.kyawzinlinn.app.alert.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing

/** A titled group of content: the section header (with optional trailing action) above its cards. */
@Composable
fun AppAlertSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    spacing: Dp = Spacing.xs,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        AppAlertSectionHeader(title, trailing = trailing)
        content()
    }
}
