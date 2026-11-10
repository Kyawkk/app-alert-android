package com.kyawzinlinn.app.alert.feature.alarm.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class ObserveAlarmSettingsUseCase(private val repository: AlarmSettingsRepository) {
    operator fun invoke(): Flow<AlarmSettings> = repository.observe()
}

class UpdateAlarmSettingsUseCase(private val repository: AlarmSettingsRepository) {
    suspend operator fun invoke(transform: (AlarmSettings) -> AlarmSettings) {
        repository.update { transform(it).sanitized() }
    }
}

/** Starts playback from the stored settings; [soundOverride] is used for per-app sounds and tests. */
class StartAlarmPlaybackUseCase(
    private val repository: AlarmSettingsRepository,
    private val player: AlarmPlayer,
) {
    suspend operator fun invoke(soundOverride: String? = null): AlarmStartResult {
        val settings = repository.observe().first().sanitized()
        return player.start(
            AlarmRequest(
                soundUri = soundOverride ?: settings.soundUri,
                volumePercent = settings.volumePercent,
                durationSeconds = settings.durationSeconds,
                repeatCount = if (settings.repeatEnabled) settings.repeatCount else 0,
                repeatIntervalSeconds = settings.repeatIntervalSeconds,
                vibrate = settings.vibrationEnabled,
            ),
        )
    }
}

class StopAlarmPlaybackUseCase(private val player: AlarmPlayer) {
    operator fun invoke() = player.stop()
}

/** Validates a picked sound before it becomes the default alarm sound. */
class SelectAlarmSoundUseCase(
    private val inspector: SoundInspector,
    private val update: UpdateAlarmSettingsUseCase,
) {
    /** @return false when the sound is unusable and nothing was saved. */
    suspend operator fun invoke(uri: String?): Boolean {
        if (uri != null && !inspector.verifyAndRetainAccess(uri)) return false
        update { it.copy(soundUri = uri) }
        return true
    }
}

class ObserveAlarmPlaybackStateUseCase(private val player: AlarmPlayer) {
    operator fun invoke(): StateFlow<Boolean> = player.isPlaying
}

class GetSoundTitleUseCase(private val inspector: SoundInspector) {
    suspend operator fun invoke(uri: String?): String = inspector.titleOf(uri)
}

/** Checks that a sound can be played (and keeps access to picked files) without saving anything. */
class VerifySoundUseCase(private val inspector: SoundInspector) {
    suspend operator fun invoke(uri: String): Boolean = inspector.verifyAndRetainAccess(uri)
}
