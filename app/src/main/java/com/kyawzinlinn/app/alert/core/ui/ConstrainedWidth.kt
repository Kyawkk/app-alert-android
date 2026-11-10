package com.kyawzinlinn.app.alert.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens

/** Centers content and caps its width so it stays readable on tablets and landscape. */
@Composable
fun ConstrainedWidth(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = Dimens.contentMaxWidth).fillMaxSize()) {
            content()
        }
    }
}
