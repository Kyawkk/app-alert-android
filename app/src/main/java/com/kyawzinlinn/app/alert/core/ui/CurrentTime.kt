package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

/**
 * "Now" that refreshes every [refreshMillis] while it is in composition, so relative times
 * ("2 min ago") stay correct. One shared value per section avoids per-item clocks and avoids
 * recomposing items with a new default time on every unrelated recomposition.
 */
@Composable
fun rememberCurrentTimeMillis(refreshMillis: Long = 60_000L): State<Long> =
    produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(refreshMillis)
            value = System.currentTimeMillis()
        }
    }
