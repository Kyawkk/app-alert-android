package com.kyawzinlinn.app.alert.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

/**
 * Hosts [content] with a bottom bar that slides and fades in and out.
 *
 * The animation runs in the layout and draw phases only: the bar slides with a graphics layer and the
 * space it takes from [content] is applied while measuring, so neither the bar nor the screen recomposes
 * on every frame. Content gets exactly the room it needs: the bar height, the navigation bar inset when
 * the bar is hidden, or the keyboard, whichever is largest.
 */
@Composable
fun AnimatedBottomBarScaffold(
    barVisible: Boolean,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = animateFloatAsState(
        targetValue = if (barVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (barVisible) 300 else 220,
            easing = FastOutSlowInEasing,
        ),
        label = "bottomBarProgress",
    )
    // flips only when the bar becomes fully hidden or starts appearing, not on every frame
    val barInComposition by remember { derivedStateOf { progress.value > 0f } }
    var barHeight by remember { mutableIntStateOf(0) }
    val navigationBars = WindowInsets.navigationBars
    val keyboard = WindowInsets.ime

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .layout { measurable, constraints ->
                    val reserved = maxOf(
                        lerp(navigationBars.getBottom(this).toFloat(), barHeight.toFloat(), progress.value).roundToInt(),
                        keyboard.getBottom(this),
                    )
                    val height = (constraints.maxHeight - reserved).coerceAtLeast(0)
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(0, 0) }
                },
        ) {
            content()
        }
        if (barVisible || barInComposition) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { barHeight = it.height }
                    .graphicsLayer {
                        translationY = (1f - progress.value) * size.height
                        alpha = progress.value
                    },
            ) {
                bottomBar()
            }
        }
    }
}
