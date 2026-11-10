package com.kyawzinlinn.app.alert.feature.alarm.presentation

sealed interface AlarmAction {
    data class SoundPicked(val uri: String?) : AlarmAction
    data object UseDefaultSound : AlarmAction
    data class VolumeChanged(val percent: Int) : AlarmAction
    data class DurationChanged(val seconds: Int) : AlarmAction
    data class RepeatEnabledChanged(val enabled: Boolean) : AlarmAction
    data class RepeatCountChanged(val count: Int) : AlarmAction
    data class RepeatIntervalChanged(val seconds: Int) : AlarmAction
    data class VibrationChanged(val enabled: Boolean) : AlarmAction
    data class CooldownChanged(val seconds: Int) : AlarmAction
    data object TestAlarm : AlarmAction
    data object StopAlarm : AlarmAction
}
