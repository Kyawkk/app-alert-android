package com.kyawzinlinn.app.alert.core.common

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Launches [block] and logs (instead of crashing the app) when storage or system calls fail, e.g. a
 * full disk during a DataStore write. Cancellation is always propagated. Never logs user content.
 */
fun CoroutineScope.launchCatching(
    tag: String = "AppAlert",
    block: suspend CoroutineScope.() -> Unit,
): Job = launch {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(tag, "Background operation failed: ${e.javaClass.simpleName}", e)
    }
}
