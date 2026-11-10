package com.kyawzinlinn.app.alert.feature.history.domain

import kotlinx.coroutines.flow.Flow

enum class AlertOutcome { TRIGGERED, PLAYBACK_FAILED, SUPPRESSED_COOLDOWN, SUPPRESSED_QUIET_HOURS }

/** History deliberately stores no notification text, only who, when and what happened. */
data class AlertRecord(
    val id: Long,
    val packageName: String,
    val timestampMillis: Long,
    val outcome: AlertOutcome,
)

interface AlertHistoryRepository {
    fun observeRecent(limit: Int): Flow<List<AlertRecord>>

    /** Stores a record and enforces the retention policy ([RETENTION_DAYS] days, [MAX_ENTRIES] entries). */
    suspend fun record(packageName: String, outcome: AlertOutcome, timestampMillis: Long)

    suspend fun clear()

    companion object {
        const val RETENTION_DAYS = 30
        const val MAX_ENTRIES = 500
    }
}

class GetNotificationHistoryUseCase(private val repository: AlertHistoryRepository) {
    operator fun invoke(limit: Int = AlertHistoryRepository.MAX_ENTRIES): Flow<List<AlertRecord>> =
        repository.observeRecent(limit)
}

class ClearHistoryUseCase(private val repository: AlertHistoryRepository) {
    suspend operator fun invoke() = repository.clear()
}
