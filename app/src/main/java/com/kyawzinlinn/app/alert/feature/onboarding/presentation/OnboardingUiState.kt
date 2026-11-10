package com.kyawzinlinn.app.alert.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.core.permissions.PermissionState

enum class OnboardingStep {
    WELCOME, HOW_IT_WORKS, NOTIFICATION_ACCESS, DND_INFO, FIRST_APP, SOUND, TEST, DONE;

    val isFirst get() = ordinal == 0
    val isLast get() = this == DONE
}

@Immutable
data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val permissions: PermissionState = PermissionState(),
    val selectedAppsCount: Int = 0,
)
