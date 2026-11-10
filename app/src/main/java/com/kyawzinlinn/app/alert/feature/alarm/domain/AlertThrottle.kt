package com.kyawzinlinn.app.alert.feature.alarm.domain

enum class ThrottleResult { ALLOWED, DUPLICATE, COOLDOWN }

/**
 * In-memory duplicate and cooldown tracking. State is intentionally not persisted:
 * after a process restart the worst case is one extra alarm, never a missed one.
 *
 * Android re-posts the same notification key for updates (progress, ranking, group changes),
 * so a callback does not always mean "new message". Same key + same content fingerprint within
 * [duplicateWindowMillis] is a duplicate.
 */
class AlertThrottle(
    private val maxTracked: Int = 256,
    private val duplicateWindowMillis: Long = 10 * 60_000L,
) {
    private class Seen(val fingerprint: Int, val atMillis: Long)

    private val seen = object : LinkedHashMap<String, Seen>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Seen>?): Boolean =
            size > maxTracked
    }
    private val lastAlarmByPackage = HashMap<String, Long>()

    /** Records the event and, when [ThrottleResult.ALLOWED], also records that an alarm is about to start. */
    @Synchronized
    fun registerAndCheck(event: NotificationEvent, cooldownSeconds: Int, nowMillis: Long): ThrottleResult {
        val fingerprint = event.fingerprint()
        val previous = seen[event.key]
        if (previous != null &&
            previous.fingerprint == fingerprint &&
            nowMillis - previous.atMillis in 0 until duplicateWindowMillis
        ) {
            return ThrottleResult.DUPLICATE
        }
        seen.remove(event.key)
        seen[event.key] = Seen(fingerprint, nowMillis)

        val last = lastAlarmByPackage[event.packageName]
        if (cooldownSeconds > 0 && last != null && nowMillis - last in 0 until cooldownSeconds * 1000L) {
            return ThrottleResult.COOLDOWN
        }
        lastAlarmByPackage[event.packageName] = nowMillis
        return ThrottleResult.ALLOWED
    }
}
