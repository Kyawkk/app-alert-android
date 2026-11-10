package com.kyawzinlinn.app.alert.feature.settings.presentation.components

import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import androidx.compose.foundation.layout.padding
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.common.ThemeMode
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSegmentedControl
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing

@Composable
fun AppearanceCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        AppAlertCardHeader("Theme")
        AppAlertSupportingText(
            "Follow the system setting, or always use light or dark.",
        )
        AppAlertSegmentedControl(
            options = ThemeMode.entries,
            selected = themeMode,
            label = { it.label() },
            onSelected = onThemeModeChange,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

@AppAlertPreviews
@Composable
private fun AppearanceCardPreview() {
    AppAlertTheme { AppearanceCard(ThemeMode.System, {}) }
}
