package com.kyawzinlinn.app.alert.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kyawzinlinn.app.alert.app.AppContainer
import com.kyawzinlinn.app.alert.app.AppRootViewModel
import com.kyawzinlinn.app.alert.feature.alarm.presentation.AlarmViewModel
import com.kyawzinlinn.app.alert.feature.applications.presentation.AppRuleDetailViewModel
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsViewModel
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardViewModel
import com.kyawzinlinn.app.alert.feature.history.presentation.HistoryViewModel
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.OnboardingViewModel
import com.kyawzinlinn.app.alert.feature.settings.presentation.SettingsViewModel

/** Creates every ViewModel from the [AppContainer] (constructor injection without a DI framework). */
class AppViewModelFactory(
    private val container: AppContainer,
    private val packageName: String? = null,
) : ViewModelProvider.Factory {

    /** Factory for [AppRuleDetailViewModel], which needs the package it configures. */
    fun forPackage(packageName: String) = AppViewModelFactory(container, packageName)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = with(container) {
        when {
            modelClass.isAssignableFrom(AppRootViewModel::class.java) -> AppRootViewModel(observeAppSettings)
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(
                observeMonitoringStatus, observeAppSettings, observeSelectedApplications, getNotificationHistory,
                observeAlarmPlaybackState, getAppLabel, setMonitoringEnabled, startAlarmPlayback, stopAlarmPlayback,
            )
            modelClass.isAssignableFrom(ApplicationsViewModel::class.java) ->
                ApplicationsViewModel(getInstalledApplications, observeSelectedApplications, updateApplicationSelection)
            modelClass.isAssignableFrom(AppRuleDetailViewModel::class.java) -> AppRuleDetailViewModel(
                packageName = requireNotNull(packageName) { "AppRuleDetailViewModel needs a package name" },
                observeSelectedApplications = observeSelectedApplications,
                observeAlarmPlaybackState = observeAlarmPlaybackState,
                getAppLabel = getAppLabel,
                getSoundTitle = getSoundTitle,
                verifySound = verifySound,
                updateAppRule = updateAppRule,
                updateSelection = updateApplicationSelection,
                startAlarmPlayback = startAlarmPlayback,
                stopAlarmPlayback = stopAlarmPlayback,
            )
            modelClass.isAssignableFrom(AlarmViewModel::class.java) -> AlarmViewModel(
                observeAlarmSettings, observeAlarmPlaybackState, getSoundTitle, updateAlarmSettings,
                selectAlarmSound, startAlarmPlayback, stopAlarmPlayback,
            )
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(getNotificationHistory, getAppLabel, clearHistory)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(observeAppSettings, permissionStatusSource, updateQuietHours)
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(permissionStatusSource, observeSelectedApplications, completeOnboarding)
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    } as T
}
