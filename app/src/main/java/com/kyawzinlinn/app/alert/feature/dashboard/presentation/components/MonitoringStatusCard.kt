package com.kyawzinlinn.app.alert.feature.dashboard.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSwitchRow
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.dashboard.domain.MonitoringStatus

private enum class Tone { Good, Neutral, Attention }

private data class StatusContent(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tone: Tone,
)

private fun MonitoringStatus.content(selectedCount: Int) = when (this) {
    MonitoringStatus.ACTIVE -> StatusContent(
        "Monitoring is on",
        "Watching $selectedCount ${if (selectedCount == 1) "app" else "apps"}. You'll hear an alarm when they notify you.",
        Icons.Default.CheckCircle, Tone.Good,
    )
    MonitoringStatus.PAUSED_BY_USER -> StatusContent(
        "Monitoring is paused", "Turn monitoring on to be alerted about new notifications.",
        Icons.Default.Info, Tone.Neutral,
    )
    MonitoringStatus.NOTIFICATION_ACCESS_MISSING -> StatusContent(
        "Notification access is off",
        "Android only lets App Alert see notifications after you allow access. No alarms will play until then.",
        Icons.Default.Warning, Tone.Attention,
    )
    MonitoringStatus.LISTENER_DISCONNECTED -> StatusContent(
        "Waiting for Android to connect",
        "Access is granted but Android has not connected App Alert yet. If this stays, switch access off and on again.",
        Icons.Default.Warning, Tone.Attention,
    )
    MonitoringStatus.NO_APPS_SELECTED -> StatusContent(
        "No apps selected", "Choose at least one app to monitor.", Icons.Default.Info, Tone.Attention,
    )
    MonitoringStatus.IN_QUIET_HOURS -> StatusContent(
        "Quiet hours", "Alarms are silent right now. Missed alerts are kept in your history.",
        Icons.Default.Info, Tone.Neutral,
    )
}

private data class TonePalette(val container: Color, val onContainer: Color, val accent: Color)

@Composable
private fun Tone.palette(): TonePalette {
    val extended = AppAlertTheme.extendedColors
    val colors = MaterialTheme.colorScheme
    return when (this) {
        Tone.Good -> TonePalette(extended.successContainer, extended.onSuccessContainer, extended.success)
        Tone.Neutral -> TonePalette(colors.surfaceVariant, colors.onSurfaceVariant, colors.primary)
        Tone.Attention -> TonePalette(extended.warningContainer, extended.onWarningContainer, extended.warning)
    }
}

@Composable
fun MonitoringStatusCard(
    status: MonitoringStatus,
    isMonitoringEnabled: Boolean,
    selectedCount: Int,
    onMonitoringChange: (Boolean) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onChooseApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = status.content(selectedCount).tone.palette()
    val container by animateColorAsState(palette.container, tween(250), label = "statusContainer")

    AppAlertCard(modifier = modifier, containerColor = container) {
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            label = "statusContent",
        ) { shownStatus ->
            val content = shownStatus.content(selectedCount)
            val shownPalette = content.tone.palette()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "${content.title}. ${content.description}"
                    },
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(shownPalette.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(content.icon, contentDescription = null, tint = shownPalette.accent, modifier = Modifier.size(26.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(content.title, style = MaterialTheme.typography.titleLarge, color = shownPalette.onContainer)
                    Text(content.description, style = MaterialTheme.typography.bodyMedium, color = shownPalette.onContainer)
                }
            }
        }
        AppAlertSwitchRow(
            title = "Monitor notifications",
            checked = isMonitoringEnabled,
            onCheckedChange = onMonitoringChange,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        when (status) {
            MonitoringStatus.NOTIFICATION_ACCESS_MISSING, MonitoringStatus.LISTENER_DISCONNECTED ->
                AppAlertButton(
                    "Open notification access", onOpenNotificationAccess,
                    Modifier.fillMaxWidth(), AppAlertButtonStyle.Primary,
                )
            MonitoringStatus.NO_APPS_SELECTED ->
                AppAlertButton("Choose apps", onChooseApps, Modifier.fillMaxWidth(), AppAlertButtonStyle.Primary)
            else -> Unit
        }
    }
}

@AppAlertPreviews
@Composable
private fun MonitoringStatusCardActivePreview() {
    AppAlertTheme {
        MonitoringStatusCard(MonitoringStatus.ACTIVE, true, 2, {}, {}, {}, Modifier.padding(Spacing.md))
    }
}

@AppAlertPreviews
@Composable
private fun MonitoringStatusCardPermissionPreview() {
    AppAlertTheme {
        MonitoringStatusCard(
            MonitoringStatus.NOTIFICATION_ACCESS_MISSING, true, 2, {}, {}, {}, Modifier.padding(Spacing.md),
        )
    }
}
