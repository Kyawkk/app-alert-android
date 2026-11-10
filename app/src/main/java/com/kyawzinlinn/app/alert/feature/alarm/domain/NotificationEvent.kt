package com.kyawzinlinn.app.alert.feature.alarm.domain

import java.util.Objects

/**
 * Minimal projection of a posted notification, containing only what rules need.
 * [toString] is redacted on purpose so content can never leak into logs.
 */
data class NotificationEvent(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val category: String?,
    val eventTimeMillis: Long,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
) {
    /** Identity of this exact content; two posts with the same fingerprint are the same event. */
    fun fingerprint(): Int = Objects.hash(title, text, eventTimeMillis)

    /** Persistent/summary notifications are not new messages (calls are the exception). */
    fun isIgnorable(): Boolean = isGroupSummary || (isOngoing && category != CATEGORY_CALL)

    override fun toString(): String = "NotificationEvent(package=$packageName)"

    companion object {
        const val CATEGORY_CALL = "call"
    }
}
