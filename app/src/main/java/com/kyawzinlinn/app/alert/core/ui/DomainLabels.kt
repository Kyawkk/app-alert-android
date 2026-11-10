package com.kyawzinlinn.app.alert.core.ui

import android.text.format.DateUtils
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome

fun AlarmFailure.userMessage(): String = when (this) {
    AlarmFailure.NO_SOUND_AVAILABLE -> "No alarm sound is available on this device. Choose a sound in alarm settings."
    AlarmFailure.SOUND_UNREADABLE -> "The selected sound could not be played. Choose another sound."
    AlarmFailure.PLAYBACK_ERROR -> "The alarm could not be played. Try again."
}

fun AlertOutcome.label(): String = when (this) {
    AlertOutcome.TRIGGERED -> "Alarm played"
    AlertOutcome.PLAYBACK_FAILED -> "Alarm failed to play"
    AlertOutcome.SUPPRESSED_COOLDOWN -> "Skipped (cooldown)"
    AlertOutcome.SUPPRESSED_QUIET_HOURS -> "Skipped (quiet hours)"
}

fun formatRelativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String =
    DateUtils.getRelativeTimeSpanString(timestampMillis, nowMillis, DateUtils.MINUTE_IN_MILLIS).toString()

fun formatMinuteOfDay(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

fun formatSeconds(seconds: Int): String = when {
    seconds < 60 -> "$seconds s"
    seconds % 60 == 0 -> "${seconds / 60} min"
    else -> "${seconds / 60} min ${seconds % 60} s"
}
