package com.kyawzinlinn.app.alert.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestination(
    val destination: NavigationDestination,
    val label: String,
    val icon: ImageVector,
)

val TopLevelDestinations = listOf(
    TopLevelDestination(NavigationDestination.Home, "Home", Icons.Default.Home),
    TopLevelDestination(NavigationDestination.Applications, "Apps", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(NavigationDestination.History, "History", Icons.Default.DateRange),
    TopLevelDestination(NavigationDestination.Settings, "Settings", Icons.Default.Settings),
)

@Composable
fun AppAlertBottomBar(
    currentRoute: String?,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier) {
        TopLevelDestinations.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.destination.route,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
fun AppAlertNavigationRail(
    currentRoute: String?,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier) {
        TopLevelDestinations.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.destination.route,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
            )
        }
    }
}
