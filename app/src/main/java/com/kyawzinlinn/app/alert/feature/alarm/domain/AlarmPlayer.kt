package com.kyawzinlinn.app.alert.feature.alarm.domain

import kotlinx.coroutines.flow.StateFlow

data class AlarmRequest(
    val soundUri: String?,
    val volumePercent: Int,
    val durationSeconds: Int,
    /** Number of additional bursts after the first one (0 = play once). */
    val repeatCount: Int,
    val repeatIntervalSeconds: Int,
    val vibrate: Boolean,
)

sealed interface AlarmStartResult {
    data object Started : AlarmStartResult
    data class Failed(val reason: AlarmFailure) : AlarmStartResult
}

enum class AlarmFailure { NO_SOUND_AVAILABLE, SOUND_UNREADABLE, PLAYBACK_ERROR }

/** Implemented by the data layer on top of Android audio APIs. Only one alarm plays at a time. */
interface AlarmPlayer {
    val isPlaying: StateFlow<Boolean>

    /** Replaces any alarm that is currently playing. */
    suspend fun start(request: AlarmRequest): AlarmStartResult

    fun stop()
}
