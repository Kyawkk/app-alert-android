package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope

/** Applies the per-application filters (keywords and categories) to a notification. */
object NotificationMatcher {
    fun matches(rule: AppRule, event: NotificationEvent): Boolean =
        matchesCategory(rule, event) && matchesKeywords(rule, event)

    private fun matchesCategory(rule: AppRule, event: NotificationEvent): Boolean =
        rule.categories.isEmpty() || event.category in rule.categories

    private fun matchesKeywords(rule: AppRule, event: NotificationEvent): Boolean {
        if (rule.keywords.isEmpty()) return true
        val haystacks = when (rule.keywordScope) {
            KeywordScope.TITLE -> listOf(event.title)
            KeywordScope.CONTENT -> listOf(event.text)
            KeywordScope.TITLE_OR_CONTENT -> listOf(event.title, event.text)
        }
        return rule.keywords.any { keyword ->
            haystacks.any { it?.contains(keyword, ignoreCase = true) == true }
        }
    }
}
