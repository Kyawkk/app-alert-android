package com.kyawzinlinn.app.alert.feature.alarm.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings

@Immutable
data class AlarmUiState(
    val isLoading: Boolean = true,
    val settings: AlarmSettings = AlarmSettings(),
    val soundTitle: String = "",
    val isPlaying: Boolean = false,
    /** The last picked sound could not be played and was not saved. */
    val soundRejected: Boolean = false,
    val playbackFailure: AlarmFailure? = null,
)
