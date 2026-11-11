package com.kyawzinlinn.app.alert.feature.update.presentation

import com.kyawzinlinn.app.alert.feature.update.domain.UpdateFailure
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateInfo

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class Available(val info: UpdateInfo) : UpdateStatus
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateStatus
    /** [hint] is set when the last install attempt could not start, for example installs are not allowed yet. */
    data class ReadyToInstall(val info: UpdateInfo, val path: String, val hint: UpdateFailure? = null) : UpdateStatus
    data class Failed(val reason: UpdateFailure) : UpdateStatus
}

data class UpdateUiState(
    val installedVersion: String = "",
    val status: UpdateStatus = UpdateStatus.Idle,
)

sealed interface UpdateAction {
    data object Check : UpdateAction
    data object Download : UpdateAction
    data object Cancel : UpdateAction
    data class InstallNotStarted(val reason: UpdateFailure) : UpdateAction
}
