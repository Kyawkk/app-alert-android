package com.kyawzinlinn.app.alert

import androidx.compose.runtime.getValue
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyawzinlinn.app.alert.app.AppContainer
import com.kyawzinlinn.app.alert.app.AppRootViewModel
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.app.navigation.AppAlertApp
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.isDark
import com.kyawzinlinn.app.alert.core.notifications.AppAlertNotificationListenerService
import com.kyawzinlinn.app.alert.core.ui.LocalAppIconLoader

private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as AppAlertApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = AppViewModelFactory(container)
        setContent {
            val rootViewModel: AppRootViewModel = viewModel(factory = factory)
            val themeMode by rootViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDark()
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose {}
            }
            AppAlertTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalAppIconLoader provides container.appIconLoader) {
                    AppAlertApp(factory)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        container.permissionStatusSource.refresh()
        val access = container.permissionStatusSource.state.value.notificationAccessGranted
        if (access && !container.listenerConnection.isConnected.value) {
            AppAlertNotificationListenerService.requestSystemRebind(this)
        }
    }
}
