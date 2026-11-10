package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle

/** The one Test / Stop button used wherever an alarm can be tried out. */
@Composable
fun AlarmTestButton(
    isPlaying: Boolean,
    onTest: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
    testText: String = "Test alarm",
    stopText: String = "Stop alarm",
) {
    val buttonModifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier
    AnimatedContent(
        targetState = isPlaying,
        modifier = modifier,
        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
        label = "alarmTestButton",
    ) { playing ->
        if (playing) {
            AppAlertButton(stopText, onStop, buttonModifier)
        } else {
            AppAlertButton(testText, onTest, buttonModifier, AppAlertButtonStyle.Tonal)
        }
    }
}
