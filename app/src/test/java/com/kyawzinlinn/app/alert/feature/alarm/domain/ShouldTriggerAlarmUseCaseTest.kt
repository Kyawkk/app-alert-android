package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.testing.FakeTimeProvider
import com.kyawzinlinn.app.alert.testing.event
import org.junit.Assert.assertEquals
import org.junit.Test

class ShouldTriggerAlarmUseCaseTest {
    private val time = FakeTimeProvider()
    private val useCase = ShouldTriggerAlarmUseCase(AlertThrottle(), time)
    private val rule = AppRule("com.viber.voip")

    private fun decide(
        notification: NotificationEvent = event(),
        rule: AppRule? = this.rule,
        monitoring: Boolean = true,
        quiet: QuietHours = QuietHours(),
        cooldown: Int = 0,
    ) = useCase(notification, rule, monitoring, quiet, cooldown)

    private fun skip(reason: SkipReason) = TriggerDecision.Skip(reason)

    @Test fun `selected app triggers`() {
        assertEquals(TriggerDecision.Trigger(null), decide())
    }

    @Test fun `per-app sound is passed through`() {
        assertEquals(TriggerDecision.Trigger("content://x"), decide(rule = rule.copy(soundUri = "content://x")))
    }

    @Test fun `global switch off skips`() {
        assertEquals(skip(SkipReason.MONITORING_DISABLED), decide(monitoring = false))
    }

    @Test fun `unselected app is ignored`() {
        assertEquals(skip(SkipReason.APP_NOT_SELECTED), decide(rule = null))
    }

    @Test fun `per-app switch off skips`() {
        assertEquals(skip(SkipReason.APP_DISABLED), decide(rule = rule.copy(enabled = false)))
    }

    @Test fun `group summaries and ongoing notifications are not new messages`() {
        assertEquals(skip(SkipReason.NOT_A_NEW_MESSAGE), decide(event(summary = true)))
        assertEquals(skip(SkipReason.NOT_A_NEW_MESSAGE), decide(event(key = "k2", ongoing = true)))
    }

    @Test fun `ongoing call notification still triggers`() {
        assertEquals(TriggerDecision.Trigger(null), decide(event(ongoing = true, category = "call")))
    }

    @Test fun `quiet hours skip`() {
        time.dayOfWeek = 2
        time.minuteOfDay = 23 * 60
        assertEquals(skip(SkipReason.QUIET_HOURS), decide(quiet = QuietHours(enabled = true)))
    }

    @Test fun `filtered notification skips and does not consume cooldown`() {
        val filtered = rule.copy(keywords = listOf("urgent"))
        assertEquals(skip(SkipReason.FILTERED_OUT), decide(event(text = "lunch"), filtered, cooldown = 60))
        assertEquals(TriggerDecision.Trigger(null), decide(event(key = "k2", text = "urgent"), filtered, cooldown = 60))
    }

    @Test fun `duplicate then cooldown`() {
        assertEquals(TriggerDecision.Trigger(null), decide(event(key = "a", text = "one"), cooldown = 60))
        assertEquals(skip(SkipReason.DUPLICATE), decide(event(key = "a", text = "one"), cooldown = 60))
        assertEquals(skip(SkipReason.COOLDOWN), decide(event(key = "b", text = "two"), cooldown = 60))
        time.now += 61_000
        assertEquals(TriggerDecision.Trigger(null), decide(event(key = "c", text = "three"), cooldown = 60))
    }
}
