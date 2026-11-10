package com.kyawzinlinn.app.alert.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kyawzinlinn.app.alert.R

val AppAlertFontFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold),
)

private val Base = Typography()
private val F = AppAlertFontFamily

internal val AppAlertTypography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    displayMedium = Base.displayMedium.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
    displaySmall = Base.displaySmall.copy(fontFamily = F, fontWeight = FontWeight.SemiBold),
    headlineLarge = Base.headlineLarge.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
    headlineMedium = Base.headlineMedium.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
    headlineSmall = Base.headlineSmall.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp),
    titleLarge = Base.titleLarge.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    titleMedium = Base.titleMedium.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    titleSmall = Base.titleSmall.copy(fontFamily = F, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
    bodyLarge = Base.bodyLarge.copy(fontFamily = F, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    bodyMedium = Base.bodyMedium.copy(fontFamily = F, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    bodySmall = Base.bodySmall.copy(fontFamily = F, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
    labelLarge = Base.labelLarge.copy(fontFamily = F, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
    labelMedium = Base.labelMedium.copy(fontFamily = F, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.2.sp),
    labelSmall = Base.labelSmall.copy(fontFamily = F, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
)
