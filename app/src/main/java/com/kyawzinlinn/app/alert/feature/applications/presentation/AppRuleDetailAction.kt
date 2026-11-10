package com.kyawzinlinn.app.alert.feature.applications.presentation

import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope

sealed interface AppRuleDetailAction {
    data class SetEnabled(val enabled: Boolean) : AppRuleDetailAction
    data class KeywordsChanged(val text: String) : AppRuleDetailAction
    data class ScopeChanged(val scope: KeywordScope) : AppRuleDetailAction
    data class CategoryToggled(val category: String) : AppRuleDetailAction
    data object SaveFilters : AppRuleDetailAction
    data class SoundPicked(val uri: String?) : AppRuleDetailAction
    data object UseGlobalSound : AppRuleDetailAction
    data object TestSound : AppRuleDetailAction
    data object StopSound : AppRuleDetailAction
    data object Remove : AppRuleDetailAction
}
