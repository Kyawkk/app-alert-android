package com.kyawzinlinn.app.alert.feature.alarm.domain

interface SoundInspector {
    suspend fun verifyAndRetainAccess(uri: String): Boolean

    suspend fun titleOf(uri: String?): String
}
