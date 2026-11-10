package com.kyawzinlinn.app.alert.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.core.common.ThemeMode
import com.kyawzinlinn.app.alert.core.permissions.PermissionState
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours

@Immutable
data class SettingsUiState(
    val isLoading: Boolean = true,
    val quietHours: QuietHours = QuietHours(),
    val permissions: PermissionState = PermissionState(),
    val themeMode: ThemeMode = ThemeMode.System,
)
