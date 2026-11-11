package com.kyawzinlinn.app.alert.feature.update.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseParserTest {

    private val sha = "ab".repeat(32)
    private val apkUrl = "https://github.com/o/r/releases/download/v1.0.12/app-alert-v1.0.12.apk"

    private fun releaseJson(apkAssetUrl: String = apkUrl) = """
        {
          "html_url": "https://github.com/o/r/releases/tag/v1.0.12",
          "assets": [
            {"name": "app-alert-v1.0.12.apk", "browser_download_url": "$apkAssetUrl", "size": 2048},
            {"name": "update.json", "browser_download_url": "https://github.com/o/r/releases/download/v1.0.12/update.json", "size": 120}
          ]
        }
    """.trimIndent()

    private fun manifest(
        versionCode: Long = 12,
        apk: String = "app-alert-v1.0.12.apk",
        hash: String = sha,
    ) = """{"versionCode":$versionCode,"versionName":"1.0.12","apk":"$apk","sha256":"$hash"}"""

    private fun parse(manifest: String, release: String = releaseJson()) =
        GitHubReleaseParser.parseUpdate(manifest, GitHubReleaseParser.parseRelease(release)!!)

    @Test
    fun readsReleaseAssets() {
        val release = GitHubReleaseParser.parseRelease(releaseJson())
        assertNotNull(release)
        assertEquals(2, release!!.assets.size)
        assertNotNull(release.asset("update.json"))
    }

    @Test
    fun validManifestProducesUpdateInfo() {
        val info = parse(manifest())
        assertNotNull(info)
        assertEquals(12L, info!!.versionCode)
        assertEquals("1.0.12", info.versionName)
        assertEquals(apkUrl, info.apkUrl)
        assertEquals(2048L, info.apkSizeBytes)
        assertEquals(sha, info.sha256)
    }

    @Test
    fun uppercaseChecksumIsNormalised() {
        assertEquals(sha, parse(manifest(hash = sha.uppercase()))!!.sha256)
    }

    @Test
    fun missingApkAssetIsRejected() {
        assertNull(parse(manifest(apk = "other.apk")))
    }

    @Test
    fun badChecksumIsRejected() {
        assertNull(parse(manifest(hash = "1234")))
    }

    @Test
    fun nonPositiveVersionCodeIsRejected() {
        assertNull(parse(manifest(versionCode = 0)))
    }

    @Test
    fun nonHttpsDownloadIsRejected() {
        assertNull(parse(manifest(), releaseJson(apkAssetUrl = apkUrl.replace("https://", "http://"))))
    }

    @Test
    fun nonApkAssetIsRejected() {
        val release = releaseJson().replace("app-alert-v1.0.12.apk", "app-alert-v1.0.12.zip")
        assertNull(parse(manifest(apk = "app-alert-v1.0.12.zip"), release))
    }

    @Test
    fun malformedJsonIsRejected() {
        assertNull(GitHubReleaseParser.parseRelease("not json"))
        val release = GitHubReleaseParser.parseRelease(releaseJson())!!
        assertNull(GitHubReleaseParser.parseUpdate("{", release))
    }
}
