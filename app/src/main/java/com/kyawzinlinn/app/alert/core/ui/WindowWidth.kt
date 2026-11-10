package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Width classes with the official Android window-size-class breakpoints (600dp / 840dp).
 * Derived from the current configuration, so split-screen and foldables update automatically.
 */
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
