package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens

@Composable
fun AppIcon(
    packageName: String,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.appIconSize,
) {
    val loader = LocalAppIconLoader.current
    // An icon that was loaded before is drawn in the first frame, so scrolling back never shows the
    // placeholder. Only an icon that has to be loaded fades in.
    val icon = remember(packageName, loader) { mutableStateOf(loader?.peek(packageName)) }
    val fadeIn = remember(packageName, loader) { icon.value == null }
    LaunchedEffect(packageName, loader) {
        if (icon.value == null) icon.value = loader?.load(packageName)
    }
    val shape = remember(size) { RoundedCornerShape(size / 4) }
    Box(modifier.size(size).clip(shape)) {
        if (fadeIn) {
            Crossfade(targetState = icon.value, animationSpec = tween(150), label = "appIcon") { loaded ->
                IconContent(loaded, label, size)
            }
        } else {
            IconContent(icon.value, label, size)
        }
    }
}

@Composable
private fun IconContent(icon: ImageBitmap?, label: String, size: Dp) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = null, // the adjacent label names the app
            modifier = Modifier.size(size),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
