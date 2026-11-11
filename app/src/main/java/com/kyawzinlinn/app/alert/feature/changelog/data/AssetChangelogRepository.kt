package com.kyawzinlinn.app.alert.feature.changelog.data

import android.content.Context
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads the changelog that is packaged inside the app, so it works offline for every installed version. */
class AssetChangelogRepository(context: Context) : ChangelogRepository {
    private val assets = context.applicationContext.assets

    override suspend fun read(): String? = withContext(Dispatchers.IO) {
        try {
            assets.open(ASSET).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }
    }

    private companion object {
        const val ASSET = "changelog.md"
    }
}
