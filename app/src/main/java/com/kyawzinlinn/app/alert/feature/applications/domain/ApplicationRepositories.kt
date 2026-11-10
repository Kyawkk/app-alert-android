package com.kyawzinlinn.app.alert.feature.applications.domain

import kotlinx.coroutines.flow.Flow

interface AppRulesRepository {
    fun observeRules(): Flow<List<AppRule>>

    suspend fun getRule(packageName: String): AppRule?

    fun isSelectedOrNull(packageName: String): Boolean?

    suspend fun upsert(rule: AppRule)

    suspend fun update(packageName: String, transform: (AppRule) -> AppRule)

    suspend fun remove(packageName: String)
}

interface InstalledAppsRepository {
    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<InstalledApp>

    suspend fun labelOf(packageName: String): String
}
