package com.kyawzinlinn.app.alert.feature.applications.presentation

import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope

sealed interface AppRuleDetailAction {
    data class SetEnabled(val enabled: Boolean) : AppRuleDetailAction
    /** One or more keywords typed or pasted by the user, separated by commas or line breaks. */
    data class KeywordsAdded(val text: String) : AppRuleDetailAction
    data class KeywordRemoved(val keyword: String) : AppRuleDetailAction
    data class ScopeChanged(val scope: KeywordScope) : AppRuleDetailAction
    data class CategoryToggled(val category: String) : AppRuleDetailAction
    /** [pendingKeyword] is text still in the keyword field, so it is not lost when saving. */
    data class SaveFilters(val pendingKeyword: String = "") : AppRuleDetailAction
    data class SoundPicked(val uri: String?) : AppRuleDetailAction
    data object UseGlobalSound : AppRuleDetailAction
    data object TestSound : AppRuleDetailAction
    data object StopSound : AppRuleDetailAction
    data object Remove : AppRuleDetailAction
}
