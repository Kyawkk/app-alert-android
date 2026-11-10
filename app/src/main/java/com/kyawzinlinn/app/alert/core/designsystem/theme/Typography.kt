package com.kyawzinlinn.app.alert.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Default = Typography()

internal val AppAlertTypography = Typography(
    headlineMedium = Default.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Default.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Default.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Default.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Default.titleSmall.copy(fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = Default.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
    // Secondary text stays readable: never below 13sp.
    bodySmall = Default.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = Default.labelMedium.copy(fontSize = 13.sp),
)
