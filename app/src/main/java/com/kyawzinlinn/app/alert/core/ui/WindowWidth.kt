package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class WindowWidth { Compact, Medium, Expanded }

@Composable
fun rememberWindowWidth(): WindowWidth {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WindowWidth.Compact
        widthDp < 840 -> WindowWidth.Medium
        else -> WindowWidth.Expanded
    }
}
