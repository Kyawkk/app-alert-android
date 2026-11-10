package com.kyawzinlinn.app.alert.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history", indices = [Index("timestampMillis")])
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestampMillis: Long,
    val outcome: String,
)

@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val enabled: Boolean,
    val soundUri: String?,
    val keywords: String,
    val keywordScope: String,
    val categories: String,
)
