package com.kyawzinlinn.app.alert.core.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kyawzinlinn.app.alert.AppAlertApplication

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP) return
        (context.applicationContext as AppAlertApplication).container.stopAlarmPlayback()
    }

    companion object {
        const val ACTION_STOP = "com.kyawzinlinn.app.alert.action.STOP_ALARM"
    }
}
