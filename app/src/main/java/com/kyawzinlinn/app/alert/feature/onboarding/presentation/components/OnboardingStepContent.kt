package com.kyawzinlinn.app.alert.feature.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertPermissionCard
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Dimens
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.permissions.PermissionState

@Composable
fun OnboardingIntroStep(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimens.heroIconSize))
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun OnboardingAccessStep(
    permissions: PermissionState,
    onOpenNotificationAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        OnboardingIntroStep(
            icon = Icons.Default.Lock,
            title = "Allow notification access",
            message = "Android will open a settings list. Find App Alert and switch it on, then come back. " +
                "App Alert only looks at apps you select and doesn't store message text.",
        )
        AppAlertPermissionCard(
            title = "Notification access",
            description = "Needed to hear about new notifications.",
            granted = permissions.notificationAccessGranted,
            actionText = "Open notification access",
            onAction = onOpenNotificationAccess,
        )
        if (!permissions.notificationAccessGranted) {
            AppAlertSupportingText(
                "You can continue without it, but no alarm will ring until access is on.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun OnboardingDndStep(
    permissions: PermissionState,
    onOpenDndAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        OnboardingIntroStep(
            icon = Icons.Default.Settings,
            title = "Do Not Disturb (optional)",
            message = "If your phone is on Do Not Disturb, Android may silence alarms. You decide whether App Alert " +
                "may ring through it; this depends on your device and is never changed automatically.",
        )
        AppAlertPermissionCard(
            title = "Do Not Disturb access",
            description = "Lets you allow App Alert in Do Not Disturb settings.",
            granted = permissions.dndPolicyAccessGranted,
            actionText = "Open Do Not Disturb access",
            onAction = onOpenDndAccess,
            optional = true,
        )
    }
}

@Composable
fun OnboardingSectionTitle(title: String, message: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(bottom = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun OnboardingDoneStep(modifier: Modifier = Modifier) {
    OnboardingIntroStep(
        icon = Icons.Default.CheckCircle,
        title = "You're all set",
        message = "App Alert will ring when your selected apps notify you. You can change apps, sounds and quiet " +
            "hours at any time.",
        modifier = modifier,
    )
}

@Composable
fun OnboardingTestButton(isPlaying: Boolean, onTest: () -> Unit, onStop: () -> Unit, modifier: Modifier = Modifier) {
    if (isPlaying) {
        AppAlertButton("Stop alarm", onStop, modifier.fillMaxWidth())
    } else {
        AppAlertButton("Play test alarm", onTest, modifier.fillMaxWidth(), AppAlertButtonStyle.Tonal)
    }
}

@AppAlertPreviews
@Composable
private fun OnboardingAccessStepPreview() {
    AppAlertTheme {
        OnboardingAccessStep(PermissionState(), {}, Modifier.padding(Spacing.md))
    }
}

@AppAlertPreviews
@Composable
private fun OnboardingWelcomePreview() {
    AppAlertTheme {
        OnboardingIntroStep(
            Icons.Default.Notifications, "Never miss an important message",
            "App Alert rings an alarm when notifications arrive from the apps you choose.",
        )
    }
}
