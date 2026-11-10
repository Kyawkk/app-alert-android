package com.kyawzinlinn.app.alert.feature.alarm.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmStartResult
import com.kyawzinlinn.app.alert.feature.alarm.domain.GetSoundTitleUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmPlaybackStateUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmSettingsUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.SelectAlarmSoundUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StartAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StopAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.UpdateAlarmSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AlarmViewModel(
    observeAlarmSettings: ObserveAlarmSettingsUseCase,
    observeAlarmPlaybackState: ObserveAlarmPlaybackStateUseCase,
    private val getSoundTitle: GetSoundTitleUseCase,
    private val updateAlarmSettings: UpdateAlarmSettingsUseCase,
    private val selectAlarmSound: SelectAlarmSoundUseCase,
    private val startAlarmPlayback: StartAlarmPlaybackUseCase,
    private val stopAlarmPlayback: StopAlarmPlaybackUseCase,
) : ViewModel() {

    private data class Transient(val soundRejected: Boolean = false, val failure: AlarmFailure? = null)

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<AlarmUiState> = combine(
        observeAlarmSettings(), observeAlarmPlaybackState(), transient,
    ) { settings, playing, extra ->
        AlarmUiState(
            isLoading = false,
            settings = settings,
            soundTitle = getSoundTitle(settings.soundUri),
            isPlaying = playing,
            soundRejected = extra.soundRejected,
            playbackFailure = extra.failure,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlarmUiState())

    fun onAction(action: AlarmAction) {
        when (action) {
            is AlarmAction.SoundPicked -> viewModelScope.launchCatching {
                val saved = selectAlarmSound(action.uri)
                transient.value = Transient(soundRejected = !saved)
            }
            AlarmAction.UseDefaultSound -> viewModelScope.launchCatching {
                selectAlarmSound(null)
                transient.value = Transient()
            }
            is AlarmAction.VolumeChanged -> update { it.copy(volumePercent = action.percent) }
            is AlarmAction.DurationChanged -> update { it.copy(durationSeconds = action.seconds) }
            is AlarmAction.RepeatEnabledChanged -> update { it.copy(repeatEnabled = action.enabled) }
            is AlarmAction.RepeatCountChanged -> update { it.copy(repeatCount = action.count) }
            is AlarmAction.RepeatIntervalChanged -> update { it.copy(repeatIntervalSeconds = action.seconds) }
            is AlarmAction.VibrationChanged -> update { it.copy(vibrationEnabled = action.enabled) }
            is AlarmAction.CooldownChanged -> update { it.copy(cooldownSeconds = action.seconds) }
            AlarmAction.TestAlarm -> viewModelScope.launchCatching {
                val failure = (startAlarmPlayback() as? AlarmStartResult.Failed)?.reason
                transient.value = transient.value.copy(failure = failure)
            }
            AlarmAction.StopAlarm -> stopAlarmPlayback()
        }
    }

    private fun update(transform: (AlarmSettings) -> AlarmSettings) {
        viewModelScope.launchCatching { updateAlarmSettings(transform) }
    }
}
