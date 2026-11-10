package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.core.common.TimeProvider
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours

enum class SkipReason {
    MONITORING_DISABLED,
    APP_NOT_SELECTED,
    APP_DISABLED,
    NOT_A_NEW_MESSAGE,
    QUIET_HOURS,
    FILTERED_OUT,
    DUPLICATE,
    COOLDOWN,
}

sealed interface TriggerDecision {
    /** [soundUri] is the per-app override, or null to use the global sound. */
    data class Trigger(val soundUri: String?) : TriggerDecision
    data class Skip(val reason: SkipReason) : TriggerDecision
}

/** Pure decision logic: every gate in the order the product defines. Duplicate/cooldown is last. */
class ShouldTriggerAlarmUseCase(
    private val throttle: AlertThrottle,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(
        event: NotificationEvent,
        rule: AppRule?,
        monitoringEnabled: Boolean,
        quietHours: QuietHours,
        cooldownSeconds: Int,
    ): TriggerDecision {
        if (!monitoringEnabled) return TriggerDecision.Skip(SkipReason.MONITORING_DISABLED)
        if (rule == null) return TriggerDecision.Skip(SkipReason.APP_NOT_SELECTED)
        if (!rule.enabled) return TriggerDecision.Skip(SkipReason.APP_DISABLED)
        if (event.isIgnorable()) return TriggerDecision.Skip(SkipReason.NOT_A_NEW_MESSAGE)
        if (QuietHoursEvaluator.isQuiet(
                quietHours, timeProvider.currentDayOfWeek(), timeProvider.currentMinuteOfDay(),
            )
        ) {
            return TriggerDecision.Skip(SkipReason.QUIET_HOURS)
        }
        if (!NotificationMatcher.matches(rule, event)) return TriggerDecision.Skip(SkipReason.FILTERED_OUT)

        return when (throttle.registerAndCheck(event, cooldownSeconds, timeProvider.nowMillis())) {
            ThrottleResult.DUPLICATE -> TriggerDecision.Skip(SkipReason.DUPLICATE)
            ThrottleResult.COOLDOWN -> TriggerDecision.Skip(SkipReason.COOLDOWN)
            ThrottleResult.ALLOWED -> TriggerDecision.Trigger(rule.soundUri)
        }
    }
}
