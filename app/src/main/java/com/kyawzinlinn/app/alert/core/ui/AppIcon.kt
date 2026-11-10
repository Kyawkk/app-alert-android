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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens

/**
 * Application icon. Shows the first letter of the label as a placeholder and cross-fades to the real
 * icon once it is loaded, so scrolling a long list never pops.
 */
@Composable
fun AppIcon(
    packageName: String,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.appIconSize,
) {
    val loader = LocalAppIconLoader.current
    val icon by produceState<ImageBitmap?>(initialValue = null, packageName, loader) {
        value = loader?.load(packageName)
    }
    val shape = RoundedCornerShape(size / 4)
    Crossfade(
        targetState = icon,
        modifier = modifier.size(size).clip(shape),
        animationSpec = tween(150),
        label = "appIcon",
    ) { loaded ->
        if (loaded != null) {
            Image(
                bitmap = loaded,
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
}
