package com.kyawzinlinn.app.alert.feature.update.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.feature.update.domain.CheckForUpdateUseCase
import com.kyawzinlinn.app.alert.feature.update.domain.DownloadResult
import com.kyawzinlinn.app.alert.feature.update.domain.DownloadUpdateUseCase
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val checkForUpdate: CheckForUpdateUseCase,
    private val downloadUpdate: DownloadUpdateUseCase,
    installedVersion: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState(installedVersion))
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    fun onAction(action: UpdateAction) {
        when (action) {
            UpdateAction.Check -> check()
            UpdateAction.Download -> download()
            UpdateAction.Cancel -> cancel()
            is UpdateAction.InstallNotStarted -> _uiState.update { state ->
                val current = state.status as? UpdateStatus.ReadyToInstall ?: return@update state
                state.copy(status = current.copy(hint = action.reason))
            }
        }
    }

    private fun check() {
        if (job?.isActive == true) return
        setStatus(UpdateStatus.Checking)
        job = viewModelScope.launch {
            val status = when (val result = checkForUpdate()) {
                UpdateCheckResult.UpToDate -> UpdateStatus.UpToDate
                is UpdateCheckResult.Available -> UpdateStatus.Available(result.info)
                is UpdateCheckResult.Failed -> UpdateStatus.Failed(result.reason)
            }
            setStatus(status)
        }
    }

    private fun download() {
        val info = (_uiState.value.status as? UpdateStatus.Available)?.info ?: return
        if (job?.isActive == true) return
        setStatus(UpdateStatus.Downloading(info, 0f))
        job = viewModelScope.launch {
            try {
                val result = downloadUpdate(info) { progress ->
                    _uiState.update { state ->
                        if (state.status is UpdateStatus.Downloading) {
                            state.copy(status = UpdateStatus.Downloading(info, progress))
                        } else {
                            state
                        }
                    }
                }
                setStatus(
                    when (result) {
                        is DownloadResult.Done -> UpdateStatus.ReadyToInstall(info, result.path)
                        is DownloadResult.Failed -> UpdateStatus.Failed(result.reason)
                    },
                )
            } catch (e: CancellationException) {
                setStatus(UpdateStatus.Available(info))
                throw e
            }
        }
    }

    private fun cancel() {
        if ((_uiState.value.status as? UpdateStatus.Downloading) != null) job?.cancel()
    }

    private fun setStatus(status: UpdateStatus) {
        _uiState.update { it.copy(status = status) }
    }
}
