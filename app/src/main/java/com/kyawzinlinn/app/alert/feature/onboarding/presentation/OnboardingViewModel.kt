package com.kyawzinlinn.app.alert.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.core.permissions.PermissionStatusSource
import com.kyawzinlinn.app.alert.feature.applications.domain.ObserveSelectedApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel(
    permissionStatusSource: PermissionStatusSource,
    observeSelectedApplications: ObserveSelectedApplicationsUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val step = MutableStateFlow(OnboardingStep.WELCOME)

    val uiState: StateFlow<OnboardingUiState> = combine(
        step, permissionStatusSource.state, observeSelectedApplications().map { it.size },
    ) { currentStep, permissions, selectedCount ->
        OnboardingUiState(currentStep, permissions, selectedCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    /** @param onFinished called after completion has been stored. */
    fun onAction(action: OnboardingAction, onFinished: () -> Unit = {}) {
        val all = OnboardingStep.entries
        when (action) {
            OnboardingAction.Next -> step.value = all.getOrElse(step.value.ordinal + 1) { step.value }
            OnboardingAction.Back -> step.value = all.getOrElse(step.value.ordinal - 1) { step.value }
            OnboardingAction.Finish -> viewModelScope.launchCatching {
                completeOnboarding()
                onFinished()
            }
        }
    }
}
