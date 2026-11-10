package com.kyawzinlinn.app.alert.core.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import com.kyawzinlinn.app.alert.feature.alarm.domain.SoundInspector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidSoundInspector(context: Context) : SoundInspector {
    private val context = context.applicationContext

    override suspend fun verifyAndRetainAccess(uri: String): Boolean = withContext(Dispatchers.IO) {
        val parsed = Uri.parse(uri)
        retainAccess(parsed)
        canPrepare(parsed)
    }

    override suspend fun titleOf(uri: String?): String = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext DEFAULT_TITLE
        val parsed = Uri.parse(uri)
        val isSystemSound = parsed.authority == "media" || parsed.authority == "settings"
        if (isSystemSound) {
            ringtoneTitle(parsed) ?: DEFAULT_CUSTOM_TITLE
        } else {
            displayName(parsed) ?: ringtoneTitle(parsed) ?: DEFAULT_CUSTOM_TITLE
        }
    }

    /** Files chosen with the document picker need a persistable grant to survive reboots. */
    private fun retainAccess(uri: Uri) {
        if (uri.scheme != "content") return
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Not a document URI (e.g. a system ringtone): no grant is needed or available.
        } catch (e: IllegalArgumentException) {
            // Same: provider does not support persistable permissions.
        }
    }

    private fun canPrepare(uri: Uri): Boolean {
        val player = MediaPlayer()
        return try {
            player.setDataSource(context, uri)
            player.prepare()
            true
        } catch (e: Exception) {
            false
        } finally {
            player.release()
        }
    }

    private fun ringtoneTitle(uri: Uri): String? =
        runCatching { RingtoneManager.getRingtone(context, uri)?.getTitle(context) }.getOrNull()

    private fun displayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    private companion object {
        const val DEFAULT_TITLE = "Default alarm sound"
        const val DEFAULT_CUSTOM_TITLE = "Custom sound"
    }
}
