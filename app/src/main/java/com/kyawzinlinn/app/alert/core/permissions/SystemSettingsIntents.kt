package com.kyawzinlinn.app.alert.core.permissions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

object SystemSettingsIntents {
    fun notificationListenerSettings() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun notificationPolicySettings() = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    fun batteryOptimizationSettings() = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    fun appNotificationSettings(context: Context) = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun appDetails(context: Context) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
}

fun Context.startSettingsIntent(intent: Intent, fallback: Intent? = null) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w("SystemSettings", "Settings screen unavailable: ${intent.action}")
        if (fallback != null) startSettingsIntent(fallback)
    }
}
