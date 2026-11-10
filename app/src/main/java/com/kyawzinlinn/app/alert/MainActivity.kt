package com.kyawzinlinn.app.alert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.kyawzinlinn.app.alert.app.AppContainer
import com.kyawzinlinn.app.alert.app.di.AppViewModelFactory
import com.kyawzinlinn.app.alert.app.navigation.AppAlertApp
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.notifications.AppAlertNotificationListenerService
import com.kyawzinlinn.app.alert.core.ui.LocalAppIconLoader

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as AppAlertApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = AppViewModelFactory(container)
        setContent {
            AppAlertTheme {
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
