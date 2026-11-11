package com.kyawzinlinn.app.alert.feature.update.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateFailure
import java.security.MessageDigest

/** Rejects a downloaded APK that is not a newer build of this app signed with the same key. */
class ApkVerifier(context: Context) {
    private val context = context.applicationContext

    /** Returns why the file must not be installed, or null when it looks like a genuine build of this app. */
    fun verify(apkPath: String, expectedVersionCode: Long): UpdateFailure? {
        val manager = context.packageManager
        val archive = archiveInfo(manager, apkPath) ?: return UpdateFailure.BAD_RESPONSE
        if (archive.packageName != context.packageName) return UpdateFailure.WRONG_PACKAGE
        if (PackageInfoCompat.getLongVersionCode(archive) != expectedVersionCode) return UpdateFailure.BAD_RESPONSE

        val downloaded = signers(archive)
        val installed = signers(installedInfo(manager))
        // when Android cannot read signing data the system installer remains the final check
        if (downloaded.isNotEmpty() && installed.isNotEmpty() && downloaded.intersect(installed).isEmpty()) {
            return UpdateFailure.WRONG_SIGNATURE
        }
        return null
    }

    private fun flags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

    private fun archiveInfo(manager: PackageManager, path: String): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(flags().toLong()))
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageArchiveInfo(path, flags())
        }

    private fun installedInfo(manager: PackageManager): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(flags().toLong()))
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageInfo(context.packageName, flags())
        }

    private fun signers(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signing = info.signingInfo ?: return emptySet()
            // includes older keys after a key rotation, so a rotated build still counts as the same app
            if (signing.hasPastSigningCertificates()) signing.signingCertificateHistory else signing.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        return signatures.orEmpty().map { sha256Hex(it.toByteArray()) }.toSet()
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
