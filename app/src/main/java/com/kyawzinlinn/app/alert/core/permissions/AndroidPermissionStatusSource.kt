package com.kyawzinlinn.app.alert.core.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPermissionStatusSource(context: Context) : PermissionStatusSource {
    private val context = context.applicationContext
    private val _state = MutableStateFlow(read())
    override val state: StateFlow<PermissionState> = _state.asStateFlow()

    override fun refresh() {
        _state.value = read()
    }

    private fun read(): PermissionState {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val powerManager = context.getSystemService(PowerManager::class.java)
        return PermissionState(
            notificationAccessGranted =
                NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
            postNotificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
            dndPolicyAccessGranted = notificationManager?.isNotificationPolicyAccessGranted == true,
            ignoringBatteryOptimizations =
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true,
        )
    }
}
