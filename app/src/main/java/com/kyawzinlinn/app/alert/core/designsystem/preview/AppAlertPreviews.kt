package com.kyawzinlinn.app.alert.core.designsystem.preview

import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark

@PreviewLightDark
@PreviewFontScale
annotation class AppAlertPreviews

object PreviewText {
    const val LONG_APP_NAME =
        "A Very Long Application Name That Should Be Truncated Gracefully In Every Layout"
    const val PACKAGE = "com.example.longpackage.name.application"
}
