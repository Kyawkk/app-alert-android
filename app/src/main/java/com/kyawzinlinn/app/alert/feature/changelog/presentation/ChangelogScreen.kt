package com.kyawzinlinn.app.alert.feature.changelog.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertEmptyState
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFilterChip
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
private val RailWidth = 24.dp
private const val FILTER_KEY = "filter"
private const val MORE_KEY = "more"

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
    val latest = remember(versions) { versions.firstOrNull()?.version }
    val hasEmpty = remember(versions) { versions.any { it.sections.isEmpty() } }
    // the installed version always stays visible so people can find where they are
    val shown = remember(versions, hideEmpty, installedVersion) {
        versions
            .filter { !hideEmpty || it.sections.isNotEmpty() || it.version == installedVersion }
            .ifEmpty { versions }
    }
    val compact = rememberWindowWidth() == WindowWidth.Compact

    Column(modifier.fillMaxSize()) {
        AppAlertTopBar(title = "Changelog", onBack = onBack)
        when {
            versions.isEmpty() -> AppAlertEmptyState(
                icon = Icons.Default.Info,
                title = "No changelog available",
                message = "This build does not include release notes.",
                modifier = Modifier.padding(Spacing.md),
            )
            compact -> Timeline(
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
        AppAlertSupportingText("$shownCount shown", Modifier.weight(1f).padding(start = Spacing.xs))
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
    // a new list on every recomposition would stop the rows below from being skipped
    val visible = remember(versions, limit) { versions.take(limit) }
    ConstrainedWidth {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = Spacing.xl),
        ) {
            if (showFilter) {
                item(key = FILTER_KEY, contentType = FILTER_KEY) {
                    FilterRow(visible.size, hideEmpty, onHideEmptyChange, Modifier.padding(bottom = Spacing.sm))
                }
            }
            itemsIndexed(visible, key = { _, version -> version.version }, contentType = { _, _ -> "version" }) { index, version ->
                TimelineItem(
                    isLast = index == visible.lastIndex,
                    highlighted = version.version == installedVersion,
                    modifier = Modifier.animateItem(),
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
            if (versions.size > limit) {
                item(key = MORE_KEY, contentType = MORE_KEY) {
                    AppAlertButton(
                        "Show ${versions.size - limit} older versions", { limit += PAGE_SIZE },
                        Modifier.fillMaxWidth().padding(top = Spacing.xs).animateItem(),
                        AppAlertButtonStyle.Tonal,
                    )
                }
            }
        }
    }
}

/** The rail is drawn, not composed, so a row costs one node instead of three and needs no intrinsic pass. */
@Composable
private fun TimelineItem(
    isLast: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dotColor = if (highlighted) colors.primary else colors.outline
    val lineColor = colors.outlineVariant
    Box(
        modifier
            .drawBehind {
                val x = RailWidth.toPx() / 2
                val radius = (if (highlighted) 7.dp else 5.dp).toPx()
                val centerY = Spacing.md.toPx() + radius
                if (!isLast) drawLine(lineColor, Offset(x, centerY), Offset(x, size.height), strokeWidth = 2.dp.toPx())
                drawCircle(dotColor, radius, Offset(x, centerY))
            }
            .padding(start = RailWidth + Spacing.xs, bottom = Spacing.sm),
    ) { content() }
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
    val selected = remember(versions, selectedVersion) {
        versions.firstOrNull { it.version == selectedVersion } ?: versions.first()
    }
    Row(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (showFilter) {
                item(key = FILTER_KEY, contentType = FILTER_KEY) {
                    FilterRow(versions.size, hideEmpty, onHideEmptyChange)
                }
            }
            items(versions, key = { it.version }, contentType = { "version" }) { version ->
                // only the row losing and the row gaining selection recompose when the selection moves
                VersionRow(
                    version = version,
                    selected = version.version == selected.version,
                    latest = version.version == latest,
                    installed = version.version == installedVersion,
                    onSelect = onSelectVersion,
                    modifier = Modifier.animateItem(),
                )
            }
        }
        Crossfade(
            targetState = selected,
            modifier = Modifier.weight(2f).fillMaxHeight(),
            animationSpec = tween(DETAIL_FADE_MILLIS),
            label = "changelogDetail",
        ) { version ->
            // scroll state lives inside, so each version opens at the top
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(end = Spacing.md)) {
                AppAlertCard(contentPadding = Spacing.xl) {
                    ChangelogVersionDetail(
                        version = version,
                        latest = version.version == latest,
                        installed = version.version == installedVersion,
                        onOpenRelease = { onOpenRelease(version) },
                        large = true,
                    )
                }
            }
        }
    }
}

private const val DETAIL_FADE_MILLIS = 180

@Composable
private fun VersionRow(
    version: ChangelogVersion,
    selected: Boolean,
    latest: Boolean,
    installed: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) colors.secondaryContainer else colors.surfaceContainerLow,
        animationSpec = tween(DETAIL_FADE_MILLIS),
        label = "versionRowColor",
    )
    val summary = remember(version) {
        listOfNotNull(version.date, changeSummary(version)).joinToString(" · ")
    }
    AppAlertCard(modifier, containerColor = container, onClick = { onSelect(version.version) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(version.version, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            VersionBadges(latest, installed)
        }
        AppAlertSupportingText(summary, Modifier.padding(top = Spacing.xxs))
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
