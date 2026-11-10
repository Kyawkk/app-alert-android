package com.kyawzinlinn.app.alert.core.designsystem.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kyawzinlinn.app.alert.core.common.ThemeMode

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object AppAlertTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

@Composable
fun AppAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = animateColorScheme(if (darkTheme) DarkColors else LightColors)
    val extended = animateExtendedColors(if (darkTheme) DarkExtendedColors else LightExtendedColors)
    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppAlertTypography,
            shapes = AppAlertShapes,
            content = content,
        )
    }
}

private const val ThemeFadeMillis = 350

@Composable
private fun animated(target: Color): Color =
    animateColorAsState(target, tween(ThemeFadeMillis), label = "themeColor").value

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme = target.copy(
    primary = animated(target.primary),
    onPrimary = animated(target.onPrimary),
    primaryContainer = animated(target.primaryContainer),
    onPrimaryContainer = animated(target.onPrimaryContainer),
    secondary = animated(target.secondary),
    onSecondary = animated(target.onSecondary),
    secondaryContainer = animated(target.secondaryContainer),
    onSecondaryContainer = animated(target.onSecondaryContainer),
    background = animated(target.background),
    onBackground = animated(target.onBackground),
    surface = animated(target.surface),
    onSurface = animated(target.onSurface),
    surfaceVariant = animated(target.surfaceVariant),
    onSurfaceVariant = animated(target.onSurfaceVariant),
    surfaceContainerLowest = animated(target.surfaceContainerLowest),
    surfaceContainerLow = animated(target.surfaceContainerLow),
    surfaceContainer = animated(target.surfaceContainer),
    surfaceContainerHigh = animated(target.surfaceContainerHigh),
    surfaceContainerHighest = animated(target.surfaceContainerHighest),
    outline = animated(target.outline),
    outlineVariant = animated(target.outlineVariant),
    error = animated(target.error),
    onError = animated(target.onError),
    errorContainer = animated(target.errorContainer),
    onErrorContainer = animated(target.onErrorContainer),
)

@Composable
private fun animateExtendedColors(target: ExtendedColors) = ExtendedColors(
    success = animated(target.success),
    onSuccess = animated(target.onSuccess),
    successContainer = animated(target.successContainer),
    onSuccessContainer = animated(target.onSuccessContainer),
    warning = animated(target.warning),
    onWarning = animated(target.onWarning),
    warningContainer = animated(target.warningContainer),
    onWarningContainer = animated(target.onWarningContainer),
)
