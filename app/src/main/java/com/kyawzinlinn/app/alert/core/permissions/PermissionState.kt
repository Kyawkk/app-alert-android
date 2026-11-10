package com.kyawzinlinn.app.alert.core.permissions

import kotlinx.coroutines.flow.StateFlow

data class PermissionState(
    val notificationAccessGranted: Boolean = false,
    val postNotificationsGranted: Boolean = true,
    val dndPolicyAccessGranted: Boolean = false,
    val ignoringBatteryOptimizations: Boolean = false,
)

interface PermissionStatusSource {
    val state: StateFlow<PermissionState>

    fun refresh()
}
