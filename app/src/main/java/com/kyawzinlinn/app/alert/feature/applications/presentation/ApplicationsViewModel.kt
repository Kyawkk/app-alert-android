package com.kyawzinlinn.app.alert.feature.applications.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyawzinlinn.app.alert.core.common.launchCatching
import com.kyawzinlinn.app.alert.feature.applications.domain.GetInstalledApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledApp
import com.kyawzinlinn.app.alert.feature.applications.domain.ObserveSelectedApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.UpdateApplicationSelectionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class ApplicationsViewModel(
    private val getInstalledApplications: GetInstalledApplicationsUseCase,
    observeSelectedApplications: ObserveSelectedApplicationsUseCase,
    private val updateApplicationSelection: UpdateApplicationSelectionUseCase,
) : ViewModel() {

    private sealed interface Installed {
        data object Loading : Installed
        data object Failed : Installed
        data class Loaded(val apps: List<InstalledApp>) : Installed
    }

    private val installed = MutableStateFlow<Installed>(Installed.Loading)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<ApplicationsUiState> = combine(
        installed, query, observeSelectedApplications(),
    ) { installedState, q, rules ->
        when (installedState) {
            Installed.Loading -> ApplicationsUiState(isLoading = true, query = q)
            Installed.Failed -> ApplicationsUiState(isLoading = false, loadFailed = true, query = q)
            is Installed.Loaded -> {
                val selected = rules.map { it.packageName }.toSet()
                val trimmed = q.trim()
                val visible = installedState.apps
                    .filter { trimmed.isEmpty() || it.label.contains(trimmed, ignoreCase = true) }
                    .map { ApplicationItemUiModel(it.packageName, it.label, it.packageName in selected) }
                    .sortedByDescending { it.isSelected }
                ApplicationsUiState(
                    isLoading = false,
                    query = q,
                    apps = visible,
                    selectedCount = selected.size,
                    totalCount = installedState.apps.size,
                )
            }
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApplicationsUiState())

    init {
        load()
    }

    fun onAction(action: ApplicationsAction) {
        when (action) {
            is ApplicationsAction.QueryChanged -> query.value = action.query
            is ApplicationsAction.SetSelected ->
                viewModelScope.launchCatching { updateApplicationSelection(action.packageName, action.selected) }
            ApplicationsAction.Retry -> load()
            ApplicationsAction.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launchCatching {
            val fresh = getInstalledApplications(forceRefresh = true)
            val current = installed.value
            if (current !is Installed.Loaded || current.apps != fresh) installed.value = Installed.Loaded(fresh)
        }
    }

    private fun load() {
        installed.value = Installed.Loading
        viewModelScope.launchCatching {
            installed.value = try {
                Installed.Loaded(getInstalledApplications())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ApplicationsViewModel", "Failed to list installed apps", e)
                Installed.Failed
            }
        }
    }
}
