package com.kyawzinlinn.app.alert.app.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.feature.alarm.presentation.AlarmRoute
import com.kyawzinlinn.app.alert.feature.applications.presentation.AppRuleDetailRoute
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsRoute
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardRoute
import com.kyawzinlinn.app.alert.feature.history.presentation.HistoryRoute
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.OnboardingRoute
import com.kyawzinlinn.app.alert.feature.settings.presentation.SettingsRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    startRoute: String,
    factory: AppViewModelFactory,
    onNavigateTopLevel: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    // detail screens slide over their parent, so they need an opaque background
    val detailBackground = Modifier.background(MaterialTheme.colorScheme.background)
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
        enterTransition = { appEnterTransition() },
        exitTransition = { appExitTransition() },
        popEnterTransition = { appPopEnterTransition() },
        popExitTransition = { appPopExitTransition() },
    ) {
        composable(NavigationDestination.Onboarding.route) {
            OnboardingRoute(
                factory = factory,
                onFinished = {
                    navController.navigate(NavigationDestination.Home.route) {
                        popUpTo(NavigationDestination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(NavigationDestination.Home.route) {
            DashboardRoute(
                factory = factory,
                onOpenApplications = { onNavigateTopLevel(NavigationDestination.Applications) },
                onOpenApp = { navController.navigate(NavigationDestination.AppDetail.createRoute(it)) },
                onOpenHistory = { onNavigateTopLevel(NavigationDestination.History) },
                onOpenSettings = { onNavigateTopLevel(NavigationDestination.Settings) },
                onOpenAlarmSettings = { navController.navigate(NavigationDestination.AlarmConfig.route) },
            )
        }
        composable(NavigationDestination.Applications.route) {
            ApplicationsRoute(
                factory = factory,
                onOpenDetails = { navController.navigate(NavigationDestination.AppDetail.createRoute(it)) },
            )
        }
        composable(
            route = NavigationDestination.AppDetail.route,
            arguments = listOf(navArgument(NavigationDestination.AppDetail.ARG_PACKAGE) { type = NavType.StringType }),
        ) { entry ->
            AppRuleDetailRoute(
                packageName = entry.arguments?.getString(NavigationDestination.AppDetail.ARG_PACKAGE).orEmpty(),
                factory = factory,
                onBack = { navController.popBackStack() },
                modifier = detailBackground,
            )
        }
        composable(NavigationDestination.History.route) { HistoryRoute(factory) }
        composable(NavigationDestination.Settings.route) {
            SettingsRoute(
                factory = factory,
                onOpenAlarmSettings = { navController.navigate(NavigationDestination.AlarmConfig.route) },
            )
        }
        composable(NavigationDestination.AlarmConfig.route) {
            AlarmRoute(factory = factory, onBack = { navController.popBackStack() }, modifier = detailBackground)
        }
    }
}
