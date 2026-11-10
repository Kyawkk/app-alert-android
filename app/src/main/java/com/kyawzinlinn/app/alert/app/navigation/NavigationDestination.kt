package com.kyawzinlinn.app.alert.app.navigation

import android.net.Uri

/** Routes. Only identifiers travel through navigation; screens load their own data. */
sealed class NavigationDestination(val route: String) {
    data object Onboarding : NavigationDestination("onboarding")
    data object Home : NavigationDestination("home")
    data object Applications : NavigationDestination("applications")
    data object History : NavigationDestination("history")
    data object Settings : NavigationDestination("settings")
    data object AlarmConfig : NavigationDestination("alarm")

    data object AppDetail : NavigationDestination("applications/{packageName}") {
        const val ARG_PACKAGE = "packageName"
        fun createRoute(packageName: String) = "applications/${Uri.encode(packageName)}"
    }
}
