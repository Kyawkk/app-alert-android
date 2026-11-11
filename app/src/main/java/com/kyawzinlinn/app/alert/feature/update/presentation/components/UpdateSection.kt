package com.kyawzinlinn.app.alert.feature.update.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertErrorText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSection
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogParser
import com.kyawzinlinn.app.alert.feature.changelog.presentation.components.ChangelogSections
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateFailure
import com.kyawzinlinn.app.alert.feature.update.domain.UpdateInfo
import com.kyawzinlinn.app.alert.feature.update.presentation.UpdateAction
import com.kyawzinlinn.app.alert.feature.update.presentation.UpdateStatus
import com.kyawzinlinn.app.alert.feature.update.presentation.UpdateUiState

@Composable
fun UpdateSection(
    state: UpdateUiState,
    onAction: (UpdateAction) -> Unit,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertSection("Updates", modifier) {
        AppAlertCard {
            AppAlertCardHeader("App Alert ${state.installedVersion}", description = description(state.status))
            val buttonModifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
            when (val status = state.status) {
                UpdateStatus.Idle, UpdateStatus.UpToDate -> AppAlertButton(
                    "Check for updates", { onAction(UpdateAction.Check) }, buttonModifier, AppAlertButtonStyle.Tonal,
                )
                UpdateStatus.Checking -> AppAlertButton(
                    "Checking...", {}, buttonModifier, AppAlertButtonStyle.Tonal, enabled = false,
                )
                is UpdateStatus.Available -> {
                    val notes = remember(status.info.notes) { ChangelogParser.parseSections(status.info.notes) }
                    if (notes.isNotEmpty()) ChangelogSections(notes, Modifier.padding(top = Spacing.sm))
                    AppAlertButton(
                        "Download ${status.info.versionName}", { onAction(UpdateAction.Download) }, buttonModifier,
                    )
                }
                is UpdateStatus.Downloading -> {
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                    )
                    AppAlertButton(
                        "Cancel", { onAction(UpdateAction.Cancel) }, buttonModifier, AppAlertButtonStyle.Outlined,
                    )
                }
                is UpdateStatus.ReadyToInstall -> {
                    status.hint?.let { AppAlertErrorText(failureMessage(it), Modifier.padding(top = Spacing.xs)) }
                    AppAlertButton("Install ${status.info.versionName}", { onInstall(status.path) }, buttonModifier)
                }
                is UpdateStatus.Failed -> {
                    AppAlertErrorText(failureMessage(status.reason), Modifier.padding(top = Spacing.xs))
                    AppAlertButton(
                        "Try again", { onAction(UpdateAction.Check) }, buttonModifier, AppAlertButtonStyle.Tonal,
                    )
                }
            }
        }
    }
}

private fun description(status: UpdateStatus): String = when (status) {
    UpdateStatus.Idle ->
        "Looks for a newer release on GitHub only when you tap the button. Nothing is sent about you or your alerts."
    UpdateStatus.Checking -> "Contacting GitHub..."
    UpdateStatus.UpToDate -> "You have the latest version."
    is UpdateStatus.Available ->
        "Version ${status.info.versionName} is available (${megabytes(status.info.apkSizeBytes)})."
    is UpdateStatus.Downloading -> "Downloading ${status.info.versionName}... ${(status.progress * 100).toInt()}%"
    is UpdateStatus.ReadyToInstall ->
        "Version ${status.info.versionName} is downloaded and verified. Android will ask you to confirm the install."
    is UpdateStatus.Failed -> "The update did not complete."
}

private fun megabytes(bytes: Long): String =
    if (bytes <= 0) "size unknown" else "%.1f MB".format(bytes / 1_000_000.0)

internal fun failureMessage(reason: UpdateFailure): String = when (reason) {
    UpdateFailure.NO_NETWORK -> "Could not reach GitHub. Check your connection and try again."
    UpdateFailure.RATE_LIMITED -> "GitHub is limiting requests right now. Try again in a few minutes."
    UpdateFailure.BAD_RESPONSE -> "The release information could not be read."
    UpdateFailure.STORAGE -> "Not enough free storage to download the update."
    UpdateFailure.CHECKSUM_MISMATCH -> "The download is damaged or was changed. It was discarded."
    UpdateFailure.WRONG_PACKAGE -> "The downloaded file is not App Alert. It was discarded."
    UpdateFailure.WRONG_SIGNATURE ->
        "The downloaded file is signed with a different key than this app, so Android would refuse it. " +
            "Uninstall and install the new version once to switch keys."
    UpdateFailure.INSTALL_NOT_ALLOWED ->
        "Allow App Alert to install apps in the screen that opened, come back and tap Install again."
    UpdateFailure.NO_INSTALLER -> "No installer is available on this device."
}

@AppAlertPreviews
@Composable
private fun UpdateSectionPreview() {
    AppAlertTheme {
        val info = UpdateInfo(12, "1.0.12", "https://example.com/a.apk", 2_000_000, "0".repeat(64), "https://example.com")
        UpdateSection(
            UpdateUiState("1.0.11", UpdateStatus.Available(info)), {}, {}, Modifier.padding(Spacing.md),
        )
    }
}
