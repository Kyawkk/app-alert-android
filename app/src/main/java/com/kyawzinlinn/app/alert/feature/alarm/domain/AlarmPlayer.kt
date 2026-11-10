package com.kyawzinlinn.app.alert.feature.alarm.domain

import kotlinx.coroutines.flow.StateFlow

data class AlarmRequest(
    val soundUri: String?,
    val volumePercent: Int,
    val durationSeconds: Int,
    val repeatCount: Int,
    val repeatIntervalSeconds: Int,
    val vibrate: Boolean,
)

sealed interface AlarmStartResult {
    data object Started : AlarmStartResult
    data class Failed(val reason: AlarmFailure) : AlarmStartResult
}

enum class AlarmFailure { NO_SOUND_AVAILABLE, SOUND_UNREADABLE, PLAYBACK_ERROR }

interface AlarmPlayer {
    val isPlaying: StateFlow<Boolean>

    suspend fun start(request: AlarmRequest): AlarmStartResult

    fun stop()
}
