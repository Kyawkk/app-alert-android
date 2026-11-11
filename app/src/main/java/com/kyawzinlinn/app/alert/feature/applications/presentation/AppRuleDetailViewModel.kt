package com.kyawzinlinn.app.alert.feature.applications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmStartResult
import com.kyawzinlinn.app.alert.feature.alarm.domain.GetSoundTitleUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmPlaybackStateUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StartAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StopAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.VerifySoundUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.GetAppLabelUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import com.kyawzinlinn.app.alert.feature.applications.domain.Keywords
import com.kyawzinlinn.app.alert.feature.applications.domain.ObserveSelectedApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.UpdateAppRuleUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.UpdateApplicationSelectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AppRuleDetailViewModel(
    private val packageName: String,
    observeSelectedApplications: ObserveSelectedApplicationsUseCase,
    observeAlarmPlaybackState: ObserveAlarmPlaybackStateUseCase,
    private val getAppLabel: GetAppLabelUseCase,
    private val getSoundTitle: GetSoundTitleUseCase,
    private val verifySound: VerifySoundUseCase,
    private val updateAppRule: UpdateAppRuleUseCase,
    private val updateSelection: UpdateApplicationSelectionUseCase,
    private val startAlarmPlayback: StartAlarmPlaybackUseCase,
    private val stopAlarmPlayback: StopAlarmPlaybackUseCase,
) : ViewModel() {

    private data class Draft(
        val keywords: List<String>,
        val scope: KeywordScope,
        val categories: Set<String>,
    )

    private data class Local(
        val draft: Draft? = null,
        val soundRejected: Boolean = false,
        val failure: AlarmFailure? = null,
    )

    private val ruleFlow = observeSelectedApplications()
        .map { rules -> rules.firstOrNull { it.packageName == packageName } }
    private val local = MutableStateFlow(Local())

    // last stored rule, so a draft edit needs no database read and shows up in the same frame
    @Volatile private var latestRule: AppRule? = null
    private var cachedLabel: String? = null
    private var cachedSound: Pair<String?, String>? = null

    val uiState: StateFlow<AppRuleDetailUiState> = combine(
        ruleFlow, local, observeAlarmPlaybackState(),
    ) { rule, extra, playing ->
        latestRule = rule
        val label = cachedLabel ?: getAppLabel(packageName).also { cachedLabel = it }
        if (rule == null) {
            AppRuleDetailUiState(isLoading = false, isSelected = false, packageName = packageName, label = label)
        } else {
            val saved = rule.toDraft()
            val draft = extra.draft ?: saved
            AppRuleDetailUiState(
                isLoading = false,
                isSelected = true,
                packageName = packageName,
                label = label,
                enabled = rule.enabled,
                soundTitle = soundTitleFor(rule.soundUri),
                soundUri = rule.soundUri,
                soundRejected = extra.soundRejected,
                keywords = draft.keywords,
                keywordScope = draft.scope,
                categories = draft.categories,
                filtersChanged = draft != saved,
                isTestPlaying = playing,
                playbackFailure = extra.failure,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppRuleDetailUiState())

    fun onAction(action: AppRuleDetailAction) {
        when (action) {
            is AppRuleDetailAction.SetEnabled -> updateRule { it.copy(enabled = action.enabled) }
            is AppRuleDetailAction.KeywordsAdded -> editDraft { it.copy(keywords = Keywords.add(it.keywords, action.text)) }
            is AppRuleDetailAction.KeywordRemoved -> editDraft { it.copy(keywords = it.keywords - action.keyword) }
            is AppRuleDetailAction.ScopeChanged -> editDraft { it.copy(scope = action.scope) }
            is AppRuleDetailAction.CategoryToggled -> editDraft {
                it.copy(
                    categories = if (action.category in it.categories) it.categories - action.category
                    else it.categories + action.category,
                )
            }
            is AppRuleDetailAction.SaveFilters -> saveFilters(action.pendingKeyword)
            is AppRuleDetailAction.SoundPicked -> pickSound(action.uri)
            AppRuleDetailAction.UseGlobalSound -> {
                local.value = local.value.copy(soundRejected = false)
                updateRule { it.copy(soundUri = null) }
            }
            AppRuleDetailAction.TestSound -> viewModelScope.launchCatching {
                val soundUri = ruleFlow.first()?.soundUri
                val failure = (startAlarmPlayback(soundUri) as? AlarmStartResult.Failed)?.reason
                local.value = local.value.copy(failure = failure)
            }
            AppRuleDetailAction.StopSound -> stopAlarmPlayback()
            AppRuleDetailAction.Remove -> viewModelScope.launchCatching { updateSelection(packageName, false) }
        }
    }

    private fun AppRule.toDraft() = Draft(keywords, keywordScope, categories)

    private fun editDraft(transform: (Draft) -> Draft) {
        val rule = latestRule ?: return
        local.update { it.copy(draft = transform(it.draft ?: rule.toDraft())) }
    }

    // the title is looked up again only when the sound itself changes, not on every filter edit
    private suspend fun soundTitleFor(uri: String?): String {
        cachedSound?.takeIf { it.first == uri }?.let { return it.second }
        val title = if (uri == null) "Uses the global alarm sound" else getSoundTitle(uri)
        cachedSound = uri to title
        return title
    }

    private fun saveFilters(pendingKeyword: String) {
        viewModelScope.launchCatching {
            val rule = ruleFlow.first() ?: return@launchCatching
            val draft = local.value.draft ?: rule.toDraft()
            val keywords = Keywords.add(draft.keywords, pendingKeyword)
            updateAppRule(packageName) {
                it.copy(keywords = keywords, keywordScope = draft.scope, categories = draft.categories)
            }
            local.value = local.value.copy(draft = null) // saved: show stored values again
        }
    }

    private fun pickSound(uri: String?) {
        viewModelScope.launchCatching {
            if (uri != null && !verifySound(uri)) {
                local.value = local.value.copy(soundRejected = true)
                return@launchCatching
            }
            local.value = local.value.copy(soundRejected = false)
            updateAppRule(packageName) { it.copy(soundUri = uri) }
        }
    }

    private fun updateRule(transform: (AppRule) -> AppRule) {
        viewModelScope.launchCatching { updateAppRule(packageName, transform) }
    }
}
