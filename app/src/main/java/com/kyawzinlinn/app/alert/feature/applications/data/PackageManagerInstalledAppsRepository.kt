package com.kyawzinlinn.app.alert.feature.applications.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledApp
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledAppsRepository
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PackageManagerInstalledAppsRepository(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : InstalledAppsRepository {
    private val context = context.applicationContext
    private val packageManager: PackageManager = this.context.packageManager

    private class Snapshot(val apps: List<InstalledApp>, val locale: Locale) {
        val packages: Set<String> = apps.mapTo(HashSet()) { it.packageName }
        val labels: Map<String, String> = apps.associate { it.packageName to it.label }
    }

    @Volatile
    private var snapshot: Snapshot? = null

    // two screens asking at once must not each load every label
    private val loadMutex = Mutex()

    private val fallbackLabels = ConcurrentHashMap<String, String>()

    override suspend fun getInstalledApps(forceRefresh: Boolean): List<InstalledApp> {
        snapshot?.takeIf { !forceRefresh }?.let { return it.apps }
        return withContext(dispatcher) {
            loadMutex.withLock {
                val current = snapshot
                if (current != null && !forceRefresh) return@withLock current.apps

                val resolved = queryLauncherActivities()
                val locale = Locale.getDefault()
                // Loading a label is the expensive part. When no app was installed or removed and the
                // language is the same, the list we already have is still right, so a refresh is cheap.
                val names = resolved.mapTo(HashSet()) { it.activityInfo.packageName }
                names.remove(context.packageName)
                if (current != null && current.locale == locale && current.packages == names) {
                    return@withLock current.apps
                }
                val fresh = Snapshot(toApps(resolved), locale)
                snapshot = fresh
                fallbackLabels.clear()
                fresh.apps
            }
        }
    }

    override suspend fun labelOf(packageName: String): String {
        snapshot?.labels?.get(packageName)?.let { return it }
        return withContext(dispatcher) {
            fallbackLabels.getOrPut(packageName) {
                try {
                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName // uninstalled since it was selected
                }
            }
        }
    }

    private fun queryLauncherActivities(): List<ResolveInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }
    }

    private fun toApps(resolved: List<ResolveInfo>): List<InstalledApp> = resolved
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { InstalledApp(it.activityInfo.packageName, safeLabel(it)) }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

    // one app with a broken label must not make the whole list fail to load
    private fun safeLabel(info: ResolveInfo): String =
        try {
            info.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            info.activityInfo.packageName
        }
}
