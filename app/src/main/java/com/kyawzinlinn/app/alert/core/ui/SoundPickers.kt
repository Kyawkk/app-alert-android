package com.kyawzinlinn.app.alert.core.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.IntentCompat

class SoundPickerActions(
    val openSystemPicker: (currentUri: String?) -> Unit,
    val openFilePicker: () -> Unit,
)

@Composable
fun rememberSoundPickerActions(onSoundPicked: (String?) -> Unit): SoundPickerActions {
    val currentOnPicked by rememberUpdatedState(onSoundPicked)

    val systemLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked = result.data?.let {
                IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            currentOnPicked(picked?.toString())
        }
    }
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) currentOnPicked(uri.toString())
    }

    return remember(systemLauncher, fileLauncher) {
        SoundPickerActions(
            openSystemPicker = { current ->
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select alarm sound")
                    current?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it)) }
                }
                launchSafely { systemLauncher.launch(intent) }
            },
            openFilePicker = { launchSafely { fileLauncher.launch(arrayOf("audio/*")) } },
        )
    }
}

private inline fun launchSafely(launch: () -> Unit) {
    try {
        launch()
    } catch (e: ActivityNotFoundException) {
        Log.w("SoundPickers", "No activity can pick sounds on this device")
    }
}
