package com.kyawzinlinn.app.alert.feature.history.data

import com.kyawzinlinn.app.alert.core.database.AlertHistoryDao
import com.kyawzinlinn.app.alert.core.database.AlertHistoryEntity
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome
import com.kyawzinlinn.app.alert.feature.history.domain.AlertRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class RoomAlertHistoryRepository(private val dao: AlertHistoryDao) : AlertHistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<AlertRecord>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun record(packageName: String, outcome: AlertOutcome, timestampMillis: Long) {
        dao.insert(AlertHistoryEntity(packageName = packageName, timestampMillis = timestampMillis, outcome = outcome.name))
        dao.deleteOlderThan(timestampMillis - TimeUnit.DAYS.toMillis(AlertHistoryRepository.RETENTION_DAYS.toLong()))
        dao.trimToNewest(AlertHistoryRepository.MAX_ENTRIES)
    }

    override suspend fun clear() = dao.deleteAll()

    private fun AlertHistoryEntity.toDomain() = AlertRecord(
        id = id,
        packageName = packageName,
        timestampMillis = timestampMillis,
        outcome = runCatching { AlertOutcome.valueOf(outcome) }.getOrDefault(AlertOutcome.TRIGGERED),
    )
}
