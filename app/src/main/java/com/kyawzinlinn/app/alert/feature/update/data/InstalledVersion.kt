package com.kyawzinlinn.app.alert.feature.update.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

class InstalledVersion(context: Context) {
    private val context = context.applicationContext

    private val info: PackageInfo by lazy {
        val packageManager = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    val code: Long get() = PackageInfoCompat.getLongVersionCode(info)
    val name: String get() = info.versionName ?: code.toString()
}
