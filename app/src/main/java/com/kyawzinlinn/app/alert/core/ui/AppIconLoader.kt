package com.kyawzinlinn.app.alert.core.ui

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppIconLoader(context: Context) {
    private val packageManager: PackageManager = context.applicationContext.packageManager
    private val cache = LruCache<String, ImageBitmap>(MAX_CACHED_ICONS)

    /** The icon if it was loaded before, so a row can draw it in its first frame. */
    fun peek(packageName: String): ImageBitmap? = cache.get(packageName)

    suspend fun load(packageName: String): ImageBitmap? {
        cache.get(packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                packageManager.getApplicationIcon(packageName).toBitmap(ICON_PX, ICON_PX).asImageBitmap()
                    .also { cache.put(packageName, it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // uninstalled, or an icon that cannot be drawn: show the letter placeholder instead of crashing
                null
            }
        }
    }

    private companion object {
        const val MAX_CACHED_ICONS = 96
        const val ICON_PX = 128
    }
}

val LocalAppIconLoader = compositionLocalOf<AppIconLoader?> { null }
