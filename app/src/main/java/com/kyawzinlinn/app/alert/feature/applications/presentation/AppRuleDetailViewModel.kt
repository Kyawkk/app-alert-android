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

/** Configuration of one monitored app. Filter edits are drafts that are saved explicitly. */
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
        val keywords: String,
        val scope: KeywordScope,
        val categories: Set<String>,
    ) {
        fun toKeywordList(): List<String> = keywords.split(',', '\n').map { it.trim() }.filter { it.isNotEmpty() }
    }

    private data class Local(
        val draft: Draft? = null,
        val soundRejected: Boolean = false,
        val failure: AlarmFailure? = null,
    )

    private val ruleFlow = observeSelectedApplications()
        .map { rules -> rules.firstOrNull { it.packageName == packageName } }
    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<AppRuleDetailUiState> = combine(
        ruleFlow, local, observeAlarmPlaybackState(),
    ) { rule, extra, playing ->
        val label = getAppLabel(packageName)
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
                soundTitle = if (rule.soundUri == null) "Uses the global alarm sound" else getSoundTitle(rule.soundUri),
                soundUri = rule.soundUri,
                soundRejected = extra.soundRejected,
                keywordsInput = draft.keywords,
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
            is AppRuleDetailAction.KeywordsChanged -> editDraft { it.copy(keywords = action.text) }
            is AppRuleDetailAction.ScopeChanged -> editDraft { it.copy(scope = action.scope) }
            is AppRuleDetailAction.CategoryToggled -> editDraft {
                it.copy(
                    categories = if (action.category in it.categories) it.categories - action.category
                    else it.categories + action.category,
                )
            }
            AppRuleDetailAction.SaveFilters -> saveFilters()
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

    private fun AppRule.toDraft() = Draft(keywords.joinToString(", "), keywordScope, categories)

    private fun editDraft(transform: (Draft) -> Draft) {
        viewModelScope.launchCatching {
            val rule = ruleFlow.first() ?: return@launchCatching
            val base = local.value.draft ?: rule.toDraft()
            local.value = local.value.copy(draft = transform(base))
        }
    }

    private fun saveFilters() {
        viewModelScope.launchCatching {
            val draft = local.value.draft ?: return@launchCatching
            updateAppRule(packageName) {
                it.copy(keywords = draft.toKeywordList(), keywordScope = draft.scope, categories = draft.categories)
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
