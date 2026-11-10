package com.kyawzinlinn.app.alert.feature.alarm.domain

interface SoundInspector {
    /** Verifies that [uri] can be played and, for user-picked files, keeps read access to it. */
    suspend fun verifyAndRetainAccess(uri: String): Boolean

    /** Human readable name of the sound; null means the default alarm sound. */
    suspend fun titleOf(uri: String?): String
}
