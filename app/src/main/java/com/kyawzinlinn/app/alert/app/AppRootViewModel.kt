package com.kyawzinlinn.app.alert.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.ThemeMode
import com.kyawzinlinn.app.alert.feature.settings.domain.ObserveAppSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppRootViewModel(observeAppSettings: ObserveAppSettingsUseCase) : ViewModel() {
    val onboardingCompleted: StateFlow<Boolean?> = observeAppSettings()
        .map { it.onboardingCompleted }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val themeMode: StateFlow<ThemeMode> = observeAppSettings()
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)
}
