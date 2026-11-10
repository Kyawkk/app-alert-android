package com.kyawzinlinn.app.alert.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF3558D6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0B1E66),
    secondary = Color(0xFF5B6478),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E4F0),
    onSecondaryContainer = Color(0xFF181D2B),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF181B22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181B22),
    surfaceVariant = Color(0xFFEDEFF6),
    onSurfaceVariant = Color(0xFF4A4F5C),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9FAFD),
    surfaceContainer = Color(0xFFF2F3F9),
    surfaceContainerHigh = Color(0xFFECEEF5),
    surfaceContainerHighest = Color(0xFFE6E8F0),
    outline = Color(0xFF727887),
    outlineVariant = Color(0xFFD4D7E2),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFB7C4FF),
    onPrimary = Color(0xFF0B2080),
    primaryContainer = Color(0xFF233BA6),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFFC3C8DA),
    onSecondary = Color(0xFF2C3040),
    secondaryContainer = Color(0xFF434859),
    onSecondaryContainer = Color(0xFFE0E4F0),
    background = Color(0xFF101218),
    onBackground = Color(0xFFE3E4EC),
    surface = Color(0xFF181B23),
    onSurface = Color(0xFFE3E4EC),
    surfaceVariant = Color(0xFF242833),
    onSurfaceVariant = Color(0xFFC4C7D4),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF14161D),
    surfaceContainer = Color(0xFF1A1D25),
    surfaceContainerHigh = Color(0xFF222530),
    surfaceContainerHighest = Color(0xFF2C303B),
    outline = Color(0xFF8D91A0),
    outlineVariant = Color(0xFF3A3E4B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    success = Color(0xFF1B7F4B),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD3F5E0),
    onSuccessContainer = Color(0xFF00391E),
    warning = Color(0xFF9A5B00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE8C7),
    onWarningContainer = Color(0xFF3A2100),
)

internal val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF7EDBA1),
    onSuccess = Color(0xFF00391E),
    successContainer = Color(0xFF0B5230),
    onSuccessContainer = Color(0xFFD3F5E0),
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF4A2A00),
    warningContainer = Color(0xFF6B4000),
    onWarningContainer = Color(0xFFFFE8C7),
)
