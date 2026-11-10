package com.kyawzinlinn.app.alert.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyawzinlinn.app.alert.app.AppRootViewModel
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.core.ui.WindowWidth
import com.kyawzinlinn.app.alert.core.ui.rememberWindowWidth

@Composable
fun AppAlertApp(factory: AppViewModelFactory) {
    val rootViewModel: AppRootViewModel = viewModel(factory = factory)
    val onboardingCompleted by rootViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val completed = onboardingCompleted
    if (completed == null) {
        Box(Modifier.fillMaxSize()) // brief blank frame while preferences load
    } else {
        AppAlertNavigation(factory, startWithOnboarding = !completed)
    }
}

@Composable
private fun AppAlertNavigation(factory: AppViewModelFactory, startWithOnboarding: Boolean) {
    val navController = rememberNavController()
    val startRoute = rememberSaveable {
        if (startWithOnboarding) NavigationDestination.Onboarding.route else NavigationDestination.Home.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showNavigation = TopLevelDestinations.any { it.destination.route == currentRoute }
    val isCompact = rememberWindowWidth() == WindowWidth.Compact

    val navigateTopLevel: (NavigationDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Row(Modifier.fillMaxSize()) {
        if (showNavigation && !isCompact) {
            AppAlertNavigationRail(currentRoute, navigateTopLevel)
        }
        Scaffold(
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showNavigation && isCompact) AppAlertBottomBar(currentRoute, navigateTopLevel)
            },
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                startRoute = startRoute,
                factory = factory,
                onNavigateTopLevel = navigateTopLevel,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
