package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

@Composable
fun rememberCurrentTimeMillis(refreshMillis: Long = 60_000L): State<Long> =
    produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(refreshMillis)
            value = System.currentTimeMillis()
        }
    }
