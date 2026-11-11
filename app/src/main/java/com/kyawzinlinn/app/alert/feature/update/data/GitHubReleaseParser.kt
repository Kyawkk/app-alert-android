package com.kyawzinlinn.app.alert.feature.update.data

import com.kyawzinlinn.app.alert.feature.update.domain.UpdateInfo
import org.json.JSONException
import org.json.JSONObject

internal data class ReleaseAsset(val name: String, val url: String, val sizeBytes: Long)

internal data class LatestRelease(val pageUrl: String, val assets: List<ReleaseAsset>) {
    fun asset(name: String): ReleaseAsset? = assets.firstOrNull { it.name == name }
}

/** Reads the GitHub "latest release" response and the update.json file attached to each release. */
internal object GitHubReleaseParser {
    private val SHA256 = Regex("^[0-9a-fA-F]{64}$")

    fun parseRelease(body: String): LatestRelease? = try {
        val json = JSONObject(body)
        val assets = json.getJSONArray("assets")
        LatestRelease(
            pageUrl = json.optString("html_url"),
            assets = (0 until assets.length()).map { index ->
                val asset = assets.getJSONObject(index)
                ReleaseAsset(
                    name = asset.getString("name"),
                    url = asset.getString("browser_download_url"),
                    sizeBytes = asset.optLong("size"),
                )
            },
        )
    } catch (e: JSONException) {
        null
    }

    fun parseUpdate(manifest: String, release: LatestRelease): UpdateInfo? = try {
        val json = JSONObject(manifest)
        val versionCode = json.getLong("versionCode")
        val sha256 = json.getString("sha256")
        val apk = release.asset(json.getString("apk"))
        val usable = apk != null &&
            apk.name.endsWith(".apk") &&
            apk.url.startsWith("https://") &&
            SHA256.matches(sha256) &&
            versionCode > 0
        if (!usable) {
            null
        } else {
            UpdateInfo(
                versionCode = versionCode,
                versionName = json.optString("versionName").ifBlank { versionCode.toString() },
                apkUrl = apk!!.url,
                apkSizeBytes = apk.sizeBytes,
                sha256 = sha256.lowercase(),
                releaseUrl = release.pageUrl,
            )
        }
    } catch (e: JSONException) {
        null
    }
}
