package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.testing.event
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertThrottleTest {

    @Test fun `first notification is allowed`() {
        assertEquals(ThrottleResult.ALLOWED, AlertThrottle().registerAndCheck(event(), 30, 0))
    }

    @Test fun `same key and content is a duplicate`() {
        val throttle = AlertThrottle()
        throttle.registerAndCheck(event(), 0, 0)
        assertEquals(ThrottleResult.DUPLICATE, throttle.registerAndCheck(event(), 0, 1_000))
    }

    @Test fun `updated content under the same key is a new event`() {
        val throttle = AlertThrottle()
        throttle.registerAndCheck(event(text = "one"), 0, 0)
        assertEquals(ThrottleResult.ALLOWED, throttle.registerAndCheck(event(text = "two"), 0, 1_000))
    }

    @Test fun `identical content is a new event after the duplicate window`() {
        val throttle = AlertThrottle(duplicateWindowMillis = 60_000)
        throttle.registerAndCheck(event(), 0, 0)
        assertEquals(ThrottleResult.ALLOWED, throttle.registerAndCheck(event(), 0, 60_000))
    }

    @Test fun `cooldown suppresses a different notification from the same app`() {
        val throttle = AlertThrottle()
        throttle.registerAndCheck(event(key = "a", text = "one"), 30, 0)
        assertEquals(ThrottleResult.COOLDOWN, throttle.registerAndCheck(event(key = "b", text = "two"), 30, 10_000))
        assertEquals(ThrottleResult.ALLOWED, throttle.registerAndCheck(event(key = "c", text = "three"), 30, 30_000))
    }

    @Test fun `cooldown is tracked per application`() {
        val throttle = AlertThrottle()
        throttle.registerAndCheck(event(key = "a", pkg = "app.one"), 30, 0)
        assertEquals(
            ThrottleResult.ALLOWED,
            throttle.registerAndCheck(event(key = "b", pkg = "app.two"), 30, 1_000),
        )
    }

    @Test fun `zero cooldown disables the cooldown`() {
        val throttle = AlertThrottle()
        throttle.registerAndCheck(event(key = "a", text = "one"), 0, 0)
        assertEquals(ThrottleResult.ALLOWED, throttle.registerAndCheck(event(key = "b", text = "two"), 0, 1))
    }

    @Test fun `tracked keys are bounded`() {
        val throttle = AlertThrottle(maxTracked = 2)
        throttle.registerAndCheck(event(key = "a"), 0, 0)
        throttle.registerAndCheck(event(key = "b"), 0, 1)
        throttle.registerAndCheck(event(key = "c"), 0, 2) // evicts "a"
        // "a" was forgotten, so the same content is treated as new again (never a missed alarm).
        assertEquals(ThrottleResult.ALLOWED, throttle.registerAndCheck(event(key = "a"), 0, 3))
    }
}
