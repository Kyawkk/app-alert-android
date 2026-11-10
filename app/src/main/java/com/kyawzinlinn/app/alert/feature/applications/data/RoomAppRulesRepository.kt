package com.kyawzinlinn.app.alert.feature.applications.data

import com.kyawzinlinn.app.alert.core.database.AppRuleDao
import com.kyawzinlinn.app.alert.core.database.AppRuleEntity
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRulesRepository
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import kotlinx.coroutines.CoroutineScope
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Rules live in Room (they are structured, per-app records). A hot in-memory snapshot, kept in
 * sync with the table, lets the notification listener decide in microseconds without a query.
 */
class RoomAppRulesRepository(
    private val dao: AppRuleDao,
    scope: CoroutineScope,
) : AppRulesRepository {

    /** null until the first database read completes. */
    private val snapshot: StateFlow<Map<String, AppRule>?> = dao.observeAll()
        .map { list -> list.map { it.toDomain() }.associateBy { it.packageName } }
        .catch { e ->
            // A broken database must not leave getRule() waiting forever: degrade to "nothing selected".
            Log.e("AppRules", "Cannot read rules: ${e.javaClass.simpleName}", e)
            emit(emptyMap())
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val writeMutex = Mutex()

    override fun observeRules(): Flow<List<AppRule>> =
        snapshot.filterNotNull().map { it.values.toList() }

    override suspend fun getRule(packageName: String): AppRule? =
        snapshot.filterNotNull().first()[packageName]

    override fun isSelectedOrNull(packageName: String): Boolean? =
        snapshot.value?.containsKey(packageName)

    override suspend fun upsert(rule: AppRule) = dao.upsert(rule.toEntity())

    override suspend fun update(packageName: String, transform: (AppRule) -> AppRule) {
        writeMutex.withLock {
            val current = dao.get(packageName)?.toDomain() ?: return
            dao.upsert(transform(current).toEntity())
        }
    }

    override suspend fun remove(packageName: String) = dao.delete(packageName)
}

private const val SEPARATOR = "\n"

internal fun AppRuleEntity.toDomain() = AppRule(
    packageName = packageName,
    enabled = enabled,
    soundUri = soundUri,
    keywords = keywords.split(SEPARATOR).filter { it.isNotEmpty() },
    keywordScope = runCatching { KeywordScope.valueOf(keywordScope) }.getOrDefault(KeywordScope.TITLE_OR_CONTENT),
    categories = categories.split(SEPARATOR).filter { it.isNotEmpty() }.toSet(),
)

internal fun AppRule.toEntity() = AppRuleEntity(
    packageName = packageName,
    enabled = enabled,
    soundUri = soundUri,
    keywords = keywords.joinToString(SEPARATOR) { it.replace(SEPARATOR, " ") },
    keywordScope = keywordScope.name,
    categories = categories.joinToString(SEPARATOR),
)
