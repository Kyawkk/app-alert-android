package com.kyawzinlinn.app.alert.feature.alarm.domain

import java.util.Objects

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
    fun fingerprint(): Int = Objects.hash(title, text, eventTimeMillis)

    fun isIgnorable(): Boolean = isGroupSummary || (isOngoing && category != CATEGORY_CALL)

    // redacted so content can never end up in logs
    override fun toString(): String = "NotificationEvent(package=$packageName)"

    companion object {
        const val CATEGORY_CALL = "call"
    }
}
