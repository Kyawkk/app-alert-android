package com.kyawzinlinn.app.alert.feature.update.data

import android.content.Context
import com.kyawzinlinn.app.alert.feature.update.domain.DownloadResult
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateFailure
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateInfo
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateLookup
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

object UpdateConfig {
    const val OWNER = "Kyawkk"
    const val REPO = "app-alert-android"

    /** Attached to every release: version code, APK file name and SHA-256 of the APK. */
    const val MANIFEST_ASSET = "update.json"
}

/** Looks for newer builds in the GitHub Releases of the project. Talks to the network only when called. */
class GitHubReleaseUpdateRepository(
    context: Context,
    private val verifier: ApkVerifier = ApkVerifier(context),
    private val owner: String = UpdateConfig.OWNER,
    private val repo: String = UpdateConfig.REPO,
) : UpdateRepository {

    private val context = context.applicationContext

    private sealed interface Http {
        data class Ok(val body: String) : Http
        data object NotFound : Http
        data object RateLimited : Http
        data object Error : Http
    }

    override suspend fun fetchLatest(): UpdateLookup = withContext(Dispatchers.IO) {
        try {
            val release = when (val response = getText("https://api.github.com/repos/$owner/$repo/releases/latest")) {
                is Http.Ok -> GitHubReleaseParser.parseRelease(response.body)
                    ?: return@withContext UpdateLookup.Failed(UpdateFailure.BAD_RESPONSE)
                Http.NotFound -> return@withContext UpdateLookup.NoRelease
                Http.RateLimited -> return@withContext UpdateLookup.Failed(UpdateFailure.RATE_LIMITED)
                Http.Error -> return@withContext UpdateLookup.Failed(UpdateFailure.BAD_RESPONSE)
            }
            // releases published before update.json existed have nothing this app can install
            val manifestAsset = release.asset(UpdateConfig.MANIFEST_ASSET)
                ?: return@withContext UpdateLookup.NoRelease
            val manifest = when (val response = getText(manifestAsset.url)) {
                is Http.Ok -> response.body
                Http.RateLimited -> return@withContext UpdateLookup.Failed(UpdateFailure.RATE_LIMITED)
                else -> return@withContext UpdateLookup.Failed(UpdateFailure.BAD_RESPONSE)
            }
            val info = GitHubReleaseParser.parseUpdate(manifest, release)
                ?: return@withContext UpdateLookup.Failed(UpdateFailure.BAD_RESPONSE)
            UpdateLookup.Found(info)
        } catch (e: IOException) {
            UpdateLookup.Failed(UpdateFailure.NO_NETWORK)
        }
    }

    override suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): DownloadResult =
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, UPDATE_DIRECTORY).apply { mkdirs() }
            directory.listFiles()?.forEach { it.delete() } // old downloads are never reused
            if (info.apkSizeBytes > 0 && directory.usableSpace < info.apkSizeBytes * 2) {
                return@withContext DownloadResult.Failed(UpdateFailure.STORAGE)
            }

            val target = File(directory, "app-alert-${info.versionCode}.apk")
            val partial = File(directory, "${target.name}.part")
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                val connection = open(info.apkUrl, ACCEPT_BINARY)
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        return@withContext DownloadResult.Failed(UpdateFailure.BAD_RESPONSE)
                    }
                    val total = connection.contentLengthLong.takeIf { it > 0 } ?: info.apkSizeBytes
                    var copied = 0L
                    var lastPercent = -1
                    connection.inputStream.use { input ->
                        partial.outputStream().use { output ->
                            val buffer = ByteArray(BUFFER_BYTES)
                            while (true) {
                                ensureActive()
                                val read = input.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                digest.update(buffer, 0, read)
                                copied += read
                                if (total > 0) {
                                    val percent = (copied * 100 / total).toInt().coerceIn(0, 100)
                                    if (percent != lastPercent) {
                                        lastPercent = percent
                                        onProgress(percent / 100f)
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    connection.disconnect()
                }

                val actual = digest.digest().joinToString("") { "%02x".format(it) }
                if (!actual.equals(info.sha256, ignoreCase = true)) {
                    return@withContext DownloadResult.Failed(UpdateFailure.CHECKSUM_MISMATCH)
                }
                if (!partial.renameTo(target)) return@withContext DownloadResult.Failed(UpdateFailure.STORAGE)

                val problem = verifier.verify(target.path, info.versionCode)
                if (problem != null) {
                    target.delete()
                    return@withContext DownloadResult.Failed(problem)
                }
                DownloadResult.Done(target.path)
            } catch (e: IOException) {
                DownloadResult.Failed(UpdateFailure.NO_NETWORK)
            } finally {
                partial.delete() // also covers cancellation; a finished file was already renamed
            }
        }

    private fun getText(url: String): Http {
        val connection = open(url, ACCEPT_JSON)
        return try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> readLimited(connection.inputStream)?.let { Http.Ok(it) } ?: Http.Error
                HttpURLConnection.HTTP_NOT_FOUND -> Http.NotFound
                HttpURLConnection.HTTP_FORBIDDEN, TOO_MANY_REQUESTS -> Http.RateLimited
                else -> Http.Error
            }
        } finally {
            connection.disconnect()
        }
    }

    // release metadata is small; anything bigger is not what we asked for
    private fun readLimited(stream: InputStream): String? = stream.use { input ->
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_BYTES)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (out.size() + read > MAX_TEXT_BYTES) return@use null
            out.write(buffer, 0, read)
        }
        String(out.toByteArray(), Charsets.UTF_8)
    }

    private fun open(url: String, accept: String): HttpURLConnection {
        require(url.startsWith("https://")) { "Updates are only fetched over HTTPS" }
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", accept)
        }
    }

    private companion object {
        const val UPDATE_DIRECTORY = "updates"
        const val USER_AGENT = "AppAlert-Updater"
        const val ACCEPT_JSON = "application/vnd.github+json, application/json"
        const val ACCEPT_BINARY = "application/octet-stream"
        const val TOO_MANY_REQUESTS = 429
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 20_000
        const val BUFFER_BYTES = 16 * 1024
        const val MAX_TEXT_BYTES = 256 * 1024
    }
}
