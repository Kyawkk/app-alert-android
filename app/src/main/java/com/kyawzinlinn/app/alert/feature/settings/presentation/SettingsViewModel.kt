package com.kyawzinlinn.app.alert.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.core.permissions.PermissionStatusSource
import com.kyawzinlinn.app.alert.feature.settings.domain.ObserveAppSettingsUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.domain.UpdateQuietHoursUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    observeAppSettings: ObserveAppSettingsUseCase,
    private val permissionStatusSource: PermissionStatusSource,
    private val updateQuietHours: UpdateQuietHoursUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        observeAppSettings(), permissionStatusSource.state,
    ) { settings, permissions ->
        SettingsUiState(isLoading = false, quietHours = settings.quietHours, permissions = permissions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.QuietHoursEnabledChanged -> edit { it.copy(enabled = action.enabled) }
            is SettingsAction.QuietStartChanged -> edit { it.copy(startMinuteOfDay = action.minuteOfDay) }
            is SettingsAction.QuietEndChanged -> edit { it.copy(endMinuteOfDay = action.minuteOfDay) }
            is SettingsAction.QuietDayToggled -> edit {
                it.copy(days = if (action.isoDay in it.days) it.days - action.isoDay else it.days + action.isoDay)
            }
            SettingsAction.RefreshPermissions -> permissionStatusSource.refresh()
        }
    }

    private fun edit(transform: (QuietHours) -> QuietHours) {
        viewModelScope.launchCatching { updateQuietHours(transform) }
    }
}
