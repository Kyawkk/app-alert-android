package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.core.common.TimeProvider
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRulesRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome
import com.kyawzinlinn.app.alert.feature.settings.domain.SettingsRepository
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** Full workflow for one incoming notification: decide, play, and record the outcome. */
class ProcessIncomingNotificationUseCase(
    private val appRules: AppRulesRepository,
    private val settings: SettingsRepository,
    private val alarmSettings: AlarmSettingsRepository,
    private val shouldTrigger: ShouldTriggerAlarmUseCase,
    private val startAlarm: StartAlarmPlaybackUseCase,
    private val history: AlertHistoryRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(event: NotificationEvent) {
        // Unselected apps exit here without touching settings, history or audio.
        val rule = appRules.getRule(event.packageName) ?: return
        val appSettings = settings.observe().first()
        val cooldown = alarmSettings.observe().first().cooldownSeconds

        when (val decision = shouldTrigger(event, rule, appSettings.monitoringEnabled, appSettings.quietHours, cooldown)) {
            is TriggerDecision.Trigger -> {
                val outcome = when (startAlarm(decision.soundUri)) {
                    AlarmStartResult.Started -> AlertOutcome.TRIGGERED
                    is AlarmStartResult.Failed -> AlertOutcome.PLAYBACK_FAILED
                }
                record(event.packageName, outcome)
            }
            is TriggerDecision.Skip -> when (decision.reason) {
                SkipReason.COOLDOWN ->
                    record(event.packageName, AlertOutcome.SUPPRESSED_COOLDOWN)
                SkipReason.QUIET_HOURS ->
                    record(event.packageName, AlertOutcome.SUPPRESSED_QUIET_HOURS)
                else -> Unit // filtered, duplicate, disabled: not user-relevant, not worth a DB write
            }
        }
    }

    /** The alarm matters more than its log entry: a failing history write must never propagate. */
    private suspend fun record(packageName: String, outcome: AlertOutcome) {
        try {
            history.record(packageName, outcome, timeProvider.nowMillis())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("ProcessNotification", "History write failed: ${e.javaClass.simpleName}")
        }
    }
}
