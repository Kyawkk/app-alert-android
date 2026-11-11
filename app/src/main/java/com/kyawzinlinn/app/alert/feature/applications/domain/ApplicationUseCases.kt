package com.kyawzinlinn.app.alert.feature.applications.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GetInstalledApplicationsUseCase(private val repository: InstalledAppsRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false): List<InstalledApp> =
        repository.getInstalledApps(forceRefresh)
}

class ObserveSelectedApplicationsUseCase(private val repository: AppRulesRepository) {
    operator fun invoke(): Flow<List<AppRule>> = repository.observeRules()
}

class UpdateApplicationSelectionUseCase(private val repository: AppRulesRepository) {
    suspend operator fun invoke(packageName: String, selected: Boolean) {
        if (selected) {
            val existing = repository.observeRules().first().firstOrNull { it.packageName == packageName }
            repository.upsert(existing?.copy(enabled = true) ?: AppRule(packageName))
        } else {
            repository.remove(packageName)
        }
    }
}

class UpdateAppRuleUseCase(private val repository: AppRulesRepository) {
    suspend operator fun invoke(packageName: String, transform: (AppRule) -> AppRule) =
        repository.update(packageName) { current ->
            val updated = transform(current)
            updated.copy(keywords = updated.keywords.map { it.trim() }.filter { it.isNotEmpty() }.distinct())
        }
}

class GetAppLabelUseCase(private val repository: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String): String = repository.labelOf(packageName)

    /** Labels for a whole list, looking each package up once however often it appears. */
    suspend fun forAll(packageNames: Collection<String>): Map<String, String> =
        packageNames.distinct().associateWith { repository.labelOf(it) }
}
