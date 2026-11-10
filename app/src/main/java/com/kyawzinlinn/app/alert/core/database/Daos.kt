package com.kyawzinlinn.app.alert.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules")
    fun observeAll(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName")
    suspend fun get(packageName: String): AppRuleEntity?

    @Upsert
    suspend fun upsert(entity: AppRuleEntity)

    @Query("DELETE FROM app_rules WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}

@Dao
interface AlertHistoryDao {
    @Query("SELECT * FROM alert_history ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<AlertHistoryEntity>>

    @Insert
    suspend fun insert(entity: AlertHistoryEntity)

    @Query("DELETE FROM alert_history WHERE timestampMillis < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long)

    @Query(
        "DELETE FROM alert_history WHERE id NOT IN " +
            "(SELECT id FROM alert_history ORDER BY timestampMillis DESC, id DESC LIMIT :maxEntries)",
    )
    suspend fun trimToNewest(maxEntries: Int)

    @Query("DELETE FROM alert_history")
    suspend fun deleteAll()
}
