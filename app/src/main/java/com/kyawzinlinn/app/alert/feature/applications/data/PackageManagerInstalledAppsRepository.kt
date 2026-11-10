package com.kyawzinlinn.app.alert.feature.applications.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledApp
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledAppsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class PackageManagerInstalledAppsRepository(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : InstalledAppsRepository {
    private val context = context.applicationContext
    private val packageManager: PackageManager = this.context.packageManager

    @Volatile
    private var cache: List<InstalledApp>? = null

    /** Labels of apps outside the launcher list (or uninstalled), so repeated lookups cost nothing. */
    private val fallbackLabels = ConcurrentHashMap<String, String>()

    override suspend fun getInstalledApps(forceRefresh: Boolean): List<InstalledApp> = withContext(dispatcher) {
        val cached = cache
        if (cached != null && !forceRefresh) {
            cached
        } else {
            load().also {
                cache = it
                fallbackLabels.clear()
            }
        }
    }

    override suspend fun labelOf(packageName: String): String = withContext(dispatcher) {
        cache?.firstOrNull { it.packageName == packageName }?.label
            ?: fallbackLabels.getOrPut(packageName) {
                try {
                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName // uninstalled since it was selected
                }
            }
    }

    private fun load(): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }
        return resolved
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(packageManager).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
