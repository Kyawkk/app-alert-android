package com.kyawzinlinn.app.alert.feature.applications.domain

enum class KeywordScope { TITLE, CONTENT, TITLE_OR_CONTENT }

data class AppRule(
    val packageName: String,
    val enabled: Boolean = true,
    val soundUri: String? = null, // null = use the global alarm sound
    val keywords: List<String> = emptyList(),
    val keywordScope: KeywordScope = KeywordScope.TITLE_OR_CONTENT,
    val categories: Set<String> = emptySet(), // Notification.CATEGORY_* values; empty = any
)

data class InstalledApp(val packageName: String, val label: String)
