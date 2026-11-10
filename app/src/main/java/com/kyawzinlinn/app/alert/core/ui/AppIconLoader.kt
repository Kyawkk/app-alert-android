package com.kyawzinlinn.app.alert.core.ui

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads launcher icons one at a time on demand (never all installed apps at once) with a small LRU cache. */
class AppIconLoader(context: Context) {
    private val packageManager: PackageManager = context.applicationContext.packageManager
    private val cache = LruCache<String, ImageBitmap>(MAX_CACHED_ICONS)

    suspend fun load(packageName: String): ImageBitmap? {
        cache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                packageManager.getApplicationIcon(packageName).toBitmap(ICON_PX, ICON_PX).asImageBitmap()
                    .also { cache.put(packageName, it) }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    private companion object {
        const val MAX_CACHED_ICONS = 96
        const val ICON_PX = 128
    }
}

/** Null in previews and tests: icons then fall back to a placeholder. */
val LocalAppIconLoader = compositionLocalOf<AppIconLoader?> { null }
