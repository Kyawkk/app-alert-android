package com.kyawzinlinn.app.alert.feature.update.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateFailure
import java.io.File

private const val APK_MIME = "application/vnd.android.package-archive"

fun updateFileAuthority(context: Context) = "${context.packageName}.updates"

/**
 * Hands the verified APK to the system installer. Android always asks the user to confirm.
 * Returns null when the installer was opened, otherwise why it was not.
 */
fun Context.startUpdateInstall(path: String): UpdateFailure? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
        val settings = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.fromParts("package", packageName, null))
        return try {
            startActivity(settings)
            UpdateFailure.INSTALL_NOT_ALLOWED
        } catch (e: ActivityNotFoundException) {
            UpdateFailure.NO_INSTALLER
        }
    }
    val file = File(path)
    if (!file.exists()) return UpdateFailure.STORAGE
    val uri = FileProvider.getUriForFile(this, updateFileAuthority(this), file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, APK_MIME)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return try {
        startActivity(intent)
        null
    } catch (e: ActivityNotFoundException) {
        UpdateFailure.NO_INSTALLER
    }
}
