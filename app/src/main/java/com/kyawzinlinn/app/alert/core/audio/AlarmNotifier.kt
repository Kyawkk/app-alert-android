package com.kyawzinlinn.app.alert.core.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kyawzinlinn.app.alert.MainActivity
import com.kyawzinlinn.app.alert.R

class AlarmNotifier(context: Context) {
    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)

    fun show() {
        ensureChannel()
        if (!manager.areNotificationsEnabled()) return // POST_NOTIFICATIONS denied: playback still works
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, StopAlarmReceiver::class.java).setAction(StopAlarmReceiver.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentIntent(openApp)
            .addAction(0, context.getString(R.string.alarm_notification_stop), stop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true) // the alarm sound is played by the app, not by this notification
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
        }
    }

    fun hide() {
        manager.cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = context.getString(R.string.alarm_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "active_alarm"
        const val NOTIFICATION_ID = 1001
    }
}
