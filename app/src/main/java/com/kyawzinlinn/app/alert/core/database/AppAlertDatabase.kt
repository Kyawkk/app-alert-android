package com.kyawzinlinn.app.alert.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlertHistoryEntity::class, AppRuleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppAlertDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun alertHistoryDao(): AlertHistoryDao

    companion object {
        fun create(context: Context): AppAlertDatabase =
            Room.databaseBuilder(context.applicationContext, AppAlertDatabase::class.java, "app_alert.db")
                .build()
    }
}
