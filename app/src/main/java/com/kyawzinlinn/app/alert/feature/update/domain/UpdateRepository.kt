package com.kyawzinlinn.app.alert.feature.update.domain

sealed interface UpdateLookup {
    data class Found(val info: UpdateInfo) : UpdateLookup
    data object NoRelease : UpdateLookup
    data class Failed(val reason: UpdateFailure) : UpdateLookup
}

sealed interface DownloadResult {
    data class Done(val path: String) : DownloadResult
    data class Failed(val reason: UpdateFailure) : DownloadResult
}

interface UpdateRepository {
    /** Reads the newest published release. Only called when the user asks for an update check. */
    suspend fun fetchLatest(): UpdateLookup

    /** Downloads the APK, checks it against the published checksum and returns its path. [onProgress] is 0..1. */
    suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): DownloadResult
}
