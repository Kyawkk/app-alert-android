package com.kyawzinlinn.app.alert.feature.alarm.domain

/**
 * User-configurable alarm behaviour.
 *
 * [durationSeconds] is how long one burst of the sound plays (looping). With [repeatEnabled]
 * the burst is repeated [repeatCount] more times, separated by [repeatIntervalSeconds].
 * [volumePercent] is relative to the device's alarm volume; the system volume is never changed.
 */
data class AlarmSettings(
    val soundUri: String? = null, // null = default system alarm sound
    val volumePercent: Int = 80,
    val durationSeconds: Int = 15,
    val repeatEnabled: Boolean = false,
    val repeatCount: Int = 2,
    val repeatIntervalSeconds: Int = 30,
    val vibrationEnabled: Boolean = true,
    val cooldownSeconds: Int = 30,
) {
    fun sanitized(): AlarmSettings = copy(
        volumePercent = volumePercent.coerceIn(VOLUME_RANGE),
        durationSeconds = durationSeconds.coerceIn(DURATION_RANGE),
        repeatCount = repeatCount.coerceIn(REPEAT_COUNT_RANGE),
        repeatIntervalSeconds = repeatIntervalSeconds.coerceIn(REPEAT_INTERVAL_RANGE),
        cooldownSeconds = cooldownSeconds.coerceIn(COOLDOWN_RANGE),
    )

    companion object {
        val VOLUME_RANGE = 0..100
        val DURATION_RANGE = 3..300
        val REPEAT_COUNT_RANGE = 1..10
        val REPEAT_INTERVAL_RANGE = 5..300
        val COOLDOWN_RANGE = 0..600
    }
}
