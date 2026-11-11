package com.kyawzinlinn.app.alert.feature.update.domain

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class Available(val info: UpdateInfo) : UpdateCheckResult
    data class Failed(val reason: UpdateFailure) : UpdateCheckResult
}

class CheckForUpdateUseCase(
    private val repository: UpdateRepository,
    private val installedVersionCode: () -> Long,
) {
    suspend operator fun invoke(): UpdateCheckResult = when (val lookup = repository.fetchLatest()) {
        is UpdateLookup.Failed -> UpdateCheckResult.Failed(lookup.reason)
        UpdateLookup.NoRelease -> UpdateCheckResult.UpToDate
        is UpdateLookup.Found ->
            if (lookup.info.versionCode > installedVersionCode()) {
                UpdateCheckResult.Available(lookup.info)
            } else {
                UpdateCheckResult.UpToDate
            }
    }
}

class DownloadUpdateUseCase(private val repository: UpdateRepository) {
    suspend operator fun invoke(info: UpdateInfo, onProgress: (Float) -> Unit): DownloadResult =
        repository.download(info, onProgress)
}
