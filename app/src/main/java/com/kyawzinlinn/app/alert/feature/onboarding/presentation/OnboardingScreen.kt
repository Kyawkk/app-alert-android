package com.kyawzinlinn.app.alert.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.ConstrainedWidth
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.components.OnboardingAccessStep
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.components.OnboardingDndStep
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.components.OnboardingDoneStep
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.components.OnboardingIntroStep
import com.kyawzinlinn.app.alert.feature.onboarding.presentation.components.OnboardingSectionTitle

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenDndAccess: () -> Unit,
    firstAppContent: @Composable () -> Unit,
    soundContent: @Composable () -> Unit,
    testContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = OnboardingStep.entries
    val progress by animateFloatAsState(
        targetValue = (state.step.ordinal + 1f) / steps.size,
        animationSpec = tween(250),
        label = "onboardingProgress",
    )
    ConstrainedWidth(modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md)
                    .semantics { contentDescription = "Step ${state.step.ordinal + 1} of ${steps.size}" },
            )
            AnimatedContent(
                targetState = state.step,
                modifier = Modifier.weight(1f).animateContentSize(),
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "onboardingStep",
            ) { step ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (step) {
                        OnboardingStep.WELCOME -> OnboardingIntroStep(
                            Icons.Default.Notifications, "Never miss an important message",
                            "App Alert rings an alarm when notifications arrive from the apps you choose, " +
                                "even if you're asleep or away from your phone.",
                        )
                        OnboardingStep.HOW_IT_WORKS -> OnboardingIntroStep(
                            Icons.Default.Info, "How it works",
                            "App Alert reads incoming notifications on your phone to spot the apps you picked. " +
                                "It stores no message text and sends nothing off your device.",
                        )
                        OnboardingStep.NOTIFICATION_ACCESS ->
                            OnboardingAccessStep(state.permissions, onOpenNotificationAccess)
                        OnboardingStep.DND_INFO -> OnboardingDndStep(state.permissions, onOpenDndAccess)
                        OnboardingStep.FIRST_APP -> Column {
                            OnboardingSectionTitle(
                                "Choose your first app",
                                if (state.selectedAppsCount == 0) "Search for an app such as Viber and switch it on."
                                else "${state.selectedAppsCount} selected. You can add more later.",
                            )
                            Box(Modifier.fillMaxWidth().padding(bottom = Spacing.sm)) { firstAppContent() }
                        }
                        OnboardingStep.SOUND -> Column {
                            OnboardingSectionTitle("Pick an alarm sound", "Choose something you can't sleep through.")
                            soundContent()
                        }
                        OnboardingStep.TEST -> Column {
                            OnboardingSectionTitle("Test your alarm", "Make sure you can hear it before you rely on it.")
                            testContent()
                        }
                        OnboardingStep.DONE -> OnboardingDoneStep()
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.step.isFirst) Box(Modifier) else {
                    AppAlertButton("Back", { onAction(OnboardingAction.Back) }, style = AppAlertButtonStyle.Text)
                }
                if (state.step.isLast) {
                    AppAlertButton("Start using App Alert", { onAction(OnboardingAction.Finish) })
                } else {
                    AppAlertButton("Continue", { onAction(OnboardingAction.Next) })
                }
            }
        }
    }
}

@AppAlertPreviews
@Composable
private fun OnboardingScreenPreview() {
    AppAlertTheme {
        OnboardingScreen(
            OnboardingUiState(step = OnboardingStep.NOTIFICATION_ACCESS), {}, {}, {}, {}, {}, {},
        )
    }
}
