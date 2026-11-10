package com.kyawzinlinn.app.alert.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens

enum class AppAlertButtonStyle { Primary, Tonal, Outlined, Text }

@Composable
fun AppAlertButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppAlertButtonStyle = AppAlertButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val sized = modifier.defaultMinSize(minHeight = Dimens.minTouchTarget)
    val label: @Composable RowScope.() -> Unit = { androidx.compose.material3.Text(text) }
    when (style) {
        AppAlertButtonStyle.Primary -> Button(onClick, sized, enabled, content = label)
        AppAlertButtonStyle.Tonal -> FilledTonalButton(onClick, sized, enabled, content = label)
        AppAlertButtonStyle.Outlined -> OutlinedButton(onClick, sized, enabled, content = label)
        AppAlertButtonStyle.Text -> TextButton(onClick, sized, enabled, content = label)
    }
}
