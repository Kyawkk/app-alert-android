package com.kyawzinlinn.app.alert.core.permissions

import kotlinx.coroutines.flow.StateFlow

data class PermissionState(
    val notificationAccessGranted: Boolean = false,
    val postNotificationsGranted: Boolean = true,
    val dndPolicyAccessGranted: Boolean = false,
    val ignoringBatteryOptimizations: Boolean = false,
)

/** Observable view of system permissions. Must be refreshed whenever the app returns to foreground. */
interface PermissionStatusSource {
    val state: StateFlow<PermissionState>

    fun refresh()
}
