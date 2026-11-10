package com.kyawzinlinn.app.alert.feature.applications.domain

import kotlinx.coroutines.flow.Flow

interface AppRulesRepository {
    fun observeRules(): Flow<List<AppRule>>

    /** Returns the rule for [packageName], waiting for the first load if needed. */
    suspend fun getRule(packageName: String): AppRule?

    /** Non-suspending hint for hot paths: true/false when known, null while still loading. */
    fun isSelectedOrNull(packageName: String): Boolean?

    suspend fun upsert(rule: AppRule)

    /** Atomically reads, transforms and stores the rule; does nothing if the app is not selected. */
    suspend fun update(packageName: String, transform: (AppRule) -> AppRule)

    suspend fun remove(packageName: String)
}

interface InstalledAppsRepository {
    /** Launchable apps except this one, sorted by label. */
    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<InstalledApp>

    /** Display label, falling back to the package name for uninstalled apps. */
    suspend fun labelOf(packageName: String): String
}
