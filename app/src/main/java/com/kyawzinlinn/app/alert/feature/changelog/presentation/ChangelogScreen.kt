package com.kyawzinlinn.app.alert.feature.changelog.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFilterChip
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertScrollableContent
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTopBar
import com.kyawzinlinn.app.alert.core.designsystem.components.ConstrainedWidth
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.core.ui.WindowWidth
import com.kyawzinlinn.app.alert.core.ui.rememberWindowWidth
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogSection
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogVersion
import com.kyawzinlinn.app.alert.feature.changelog.presentation.components.ChangelogVersionDetail
import com.kyawzinlinn.app.alert.feature.changelog.presentation.components.VersionBadges
import com.kyawzinlinn.app.alert.feature.changelog.presentation.components.changeSummary

private const val PAGE_SIZE = 6

@Composable
fun ChangelogScreen(
    versions: List<ChangelogVersion>,
    installedVersion: String,
    selectedVersion: String?,
    onSelectVersion: (String) -> Unit,
    onOpenRelease: (ChangelogVersion) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hideEmpty by rememberSaveable { mutableStateOf(true) }
    val latest = versions.firstOrNull()?.version
    // the installed version always stays visible so people can find where they are
    val shown = versions
        .filter { !hideEmpty || it.sections.isNotEmpty() || it.version == installedVersion }
        .ifEmpty { versions }
    val hasEmpty = versions.any { it.sections.isEmpty() }

    Column(modifier.fillMaxSize()) {
        AppAlertTopBar(title = "Changelog", onBack = onBack)
        when {
            versions.isEmpty() -> AppAlertEmptyState(
                icon = Icons.Default.Info,
                title = "No changelog available",
                message = "This build does not include release notes.",
                modifier = Modifier.padding(Spacing.md),
            )
            rememberWindowWidth() == WindowWidth.Compact -> Timeline(
                versions = shown, installedVersion = installedVersion, latest = latest,
                hideEmpty = hideEmpty, showFilter = hasEmpty, onHideEmptyChange = { hideEmpty = it },
                onOpenRelease = onOpenRelease,
            )
            else -> TwoPane(
                versions = shown, installedVersion = installedVersion, latest = latest,
                selectedVersion = selectedVersion, onSelectVersion = onSelectVersion,
                hideEmpty = hideEmpty, showFilter = hasEmpty, onHideEmptyChange = { hideEmpty = it },
                onOpenRelease = onOpenRelease,
            )
        }
    }
}

@Composable
private fun FilterRow(
    shownCount: Int,
    hideEmpty: Boolean,
    onHideEmptyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AppAlertFilterChip(hideEmpty, { onHideEmptyChange(!hideEmpty) }, "Only versions with changes")
        AppAlertSupportingText(
            "$shownCount shown",
            Modifier.weight(1f).padding(start = Spacing.xs),
        )
    }
}

/** Phone layout: a vertical timeline, newest first, with older versions behind a button. */
@Composable
private fun Timeline(
    versions: List<ChangelogVersion>,
    installedVersion: String,
    latest: String?,
    hideEmpty: Boolean,
    showFilter: Boolean,
    onHideEmptyChange: (Boolean) -> Unit,
    onOpenRelease: (ChangelogVersion) -> Unit,
) {
    var limit by rememberSaveable { mutableIntStateOf(PAGE_SIZE) }
    val visible = versions.take(limit)
    ConstrainedWidth {
        AppAlertScrollableContent {
            if (showFilter) FilterRow(visible.size, hideEmpty, onHideEmptyChange)
            Column {
                visible.forEachIndexed { index, version ->
                    TimelineItem(
                        isLast = index == visible.lastIndex,
                        highlighted = version.version == installedVersion,
                    ) {
                        AppAlertCard {
                            ChangelogVersionDetail(
                                version = version,
                                latest = version.version == latest,
                                installed = version.version == installedVersion,
                                onOpenRelease = { onOpenRelease(version) },
                            )
                        }
                    }
                }
            }
            if (versions.size > limit) {
                AppAlertButton(
                    "Show ${versions.size - limit} older versions", { limit += PAGE_SIZE },
                    Modifier.fillMaxWidth(), AppAlertButtonStyle.Tonal,
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(isLast: Boolean, highlighted: Boolean, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.width(24.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .padding(top = Spacing.md)
                    .size(if (highlighted) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(if (highlighted) colors.primary else colors.outline),
            )
            if (!isLast) Box(Modifier.width(2.dp).weight(1f).background(colors.outlineVariant))
        }
        Box(Modifier.weight(1f).padding(start = Spacing.xs, bottom = Spacing.sm)) { content() }
    }
}

/** Tablet layout: version list on the left, the selected version's notes on the right. */
@Composable
private fun TwoPane(
    versions: List<ChangelogVersion>,
    installedVersion: String,
    latest: String?,
    selectedVersion: String?,
    onSelectVersion: (String) -> Unit,
    hideEmpty: Boolean,
    showFilter: Boolean,
    onHideEmptyChange: (Boolean) -> Unit,
    onOpenRelease: (ChangelogVersion) -> Unit,
) {
    val selected = versions.firstOrNull { it.version == selectedVersion } ?: versions.first()
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (showFilter) FilterRow(versions.size, hideEmpty, onHideEmptyChange)
            versions.forEach { version ->
                val isSelected = version.version == selected.version
                AppAlertCard(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    onClick = { onSelectVersion(version.version) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            version.version, Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        VersionBadges(version.version == latest, version.version == installedVersion)
                    }
                    AppAlertSupportingText(
                        listOfNotNull(version.date, changeSummary(version)).joinToString(" · "),
                        Modifier.padding(top = Spacing.xxs),
                    )
                }
            }
        }
        Column(
            Modifier.weight(2f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(end = Spacing.md),
        ) {
            AppAlertCard(contentPadding = Spacing.xl) {
                ChangelogVersionDetail(
                    version = selected,
                    latest = selected.version == latest,
                    installed = selected.version == installedVersion,
                    onOpenRelease = { onOpenRelease(selected) },
                    large = true,
                )
            }
        }
    }
}

private val previewVersions = listOf(
    ChangelogVersion(
        "1.0.19", "2026-11-12",
        listOf(
            ChangelogSection("Added", listOf("update: check and install updates from GitHub Releases")),
            ChangelogSection("Fixed", listOf("build: stop repackaging classes", "plain entry")),
        ),
    ),
    ChangelogVersion("1.0.18", "2026-11-11", emptyList()),
    ChangelogVersion("1.0.17", "2026-11-11", listOf(ChangelogSection("Performance", listOf("ui: smoother list scrolling")))),
)

@AppAlertPreviews
@Composable
private fun ChangelogScreenPreview() {
    AppAlertTheme { ChangelogScreen(previewVersions, "1.0.17", null, {}, {}, {}) }
}
