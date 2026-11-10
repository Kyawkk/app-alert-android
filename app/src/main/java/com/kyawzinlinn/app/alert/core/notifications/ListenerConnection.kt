package com.kyawzinlinn.app.alert.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the system has actually bound our NotificationListenerService.
 * Permission alone does not prove monitoring works, so the UI combines both.
 */
class ListenerConnection {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
