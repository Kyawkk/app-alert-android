package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.compose.runtime.Immutable
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope

@Immutable
data class AppRuleDetailUiState(
    val isLoading: Boolean = true,
    /** False when the app is not (or no longer) selected. */
    val isSelected: Boolean = false,
    val packageName: String = "",
    val label: String = "",
    val enabled: Boolean = true,
    val soundTitle: String = "",
    val soundUri: String? = null,
    val soundRejected: Boolean = false,
    val keywordsInput: String = "",
    val keywordScope: KeywordScope = KeywordScope.TITLE_OR_CONTENT,
    val categories: Set<String> = emptySet(),
    val filtersChanged: Boolean = false,
    val isTestPlaying: Boolean = false,
    val playbackFailure: AlarmFailure? = null,
)
