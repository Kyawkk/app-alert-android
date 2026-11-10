package com.kyawzinlinn.app.alert.core.notifications

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.kyawzinlinn.app.alert.AppAlertApplication
import com.kyawzinlinn.app.alert.feature.alarm.domain.NotificationEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppAlertNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val container get() = (application as AppAlertApplication).container

    override fun onListenerConnected() {
        container.listenerConnection.setConnected(true)
    }

    override fun onListenerDisconnected() {
        container.listenerConnection.setConnected(false)
        requestSystemRebind(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return // never react to our own "active alarm" notification
        if (container.appRulesRepository.isSelectedOrNull(sbn.packageName) == false) return

        val event = sbn.toEvent()
        serviceScope.launch {
            try {
                container.processIncomingNotification(event)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process notification from ${event.packageName}", e)
            }
        }
    }

    override fun onDestroy() {
        container.listenerConnection.setConnected(false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun StatusBarNotification.toEvent(): NotificationEvent {
        val notification = notification
        val extras = notification.extras
        return NotificationEvent(
            key = key,
            packageName = packageName,
            title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = (extras?.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString(),
            category = notification.category,
            eventTimeMillis = notification.`when`,
            isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0,
            isGroupSummary = notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
        )
    }

    companion object {
        private const val TAG = "AppAlertListener"

        fun requestSystemRebind(context: Context) {
            try {
                NotificationListenerService.requestRebind(
                    ComponentName(context, AppAlertNotificationListenerService::class.java),
                )
            } catch (e: Exception) {
                Log.w(TAG, "requestRebind failed: ${e.javaClass.simpleName}")
            }
        }
    }
}
