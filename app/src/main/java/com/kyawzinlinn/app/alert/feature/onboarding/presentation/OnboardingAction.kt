package com.kyawzinlinn.app.alert.feature.onboarding.presentation

sealed interface OnboardingAction {
    data object Next : OnboardingAction
    data object Back : OnboardingAction
    data object Finish : OnboardingAction
}
