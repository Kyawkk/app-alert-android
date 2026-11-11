package com.kyawzinlinn.app.alert.feature.changelog.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButtonStyle
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSection
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogSection
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogVersion

/** A small rounded label. */
@Composable
fun ChangelogTag(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .background(container, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = content,
    )
}

@Composable
private fun SectionTag(title: String) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (title) {
        "Added" -> colors.primaryContainer to colors.onPrimaryContainer
        "Fixed" -> colors.tertiaryContainer to colors.onTertiaryContainer
        "Performance" -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHighest to colors.onSurfaceVariant
    }
    ChangelogTag(title, container, content)
}

/** "Latest" and "Installed" markers for a version. */
@Composable
fun VersionBadges(latest: Boolean, installed: Boolean, modifier: Modifier = Modifier) {
    if (!latest && !installed) return
    val colors = MaterialTheme.colorScheme
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        if (latest) ChangelogTag("Latest", colors.inverseSurface, colors.inverseOnSurface)
        if (installed) ChangelogTag("Installed", colors.primary, colors.onPrimary)
    }
}

private val Scoped = Regex("^([a-z][\\w-]*): (.+)$")

@Composable
private fun ChangeItem(text: String) {
    val colors = MaterialTheme.colorScheme
    val scopeColor = colors.onSurface
    // built once per entry instead of on every recomposition
    val styled = remember(text, scopeColor) {
        val match = Scoped.find(text)
        buildAnnotatedString {
            if (match != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = scopeColor)) {
                    append(match.groupValues[1])
                }
                append(": ")
                append(match.groupValues[2].replaceFirstChar { it.uppercase() })
            } else {
                append(text.replaceFirstChar { it.uppercase() })
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text("•", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Text(
            styled, Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
        )
    }
}

/** Tagged groups of changes, shared by the changelog screen and the update card. */
@Composable
fun ChangelogSections(sections: List<ChangelogSection>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        sections.forEach { section ->
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                SectionTag(section.title)
                section.items.forEach { ChangeItem(it) }
            }
        }
    }
}

/** The entry in Settings that opens the changelog screen. */
@Composable
fun ChangelogSettingsSection(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    AppAlertSection("What's new", modifier) {
        AppAlertCard {
            AppAlertCardHeader("Changelog", description = "What changed in each version of App Alert.")
            AppAlertButton(
                "View changelog", onOpen,
                Modifier.fillMaxWidth().padding(top = Spacing.sm), AppAlertButtonStyle.Tonal,
            )
        }
    }
}

/** The notes of one version with a link to its GitHub release. */
@Composable
fun ChangelogVersionDetail(
    version: ChangelogVersion,
    latest: Boolean,
    installed: Boolean,
    onOpenRelease: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                "Version ${version.version}",
                style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            )
            VersionBadges(latest, installed)
        }
        version.date?.let { AppAlertSupportingText(it) }
        if (version.sections.isEmpty()) {
            AppAlertSupportingText("No user-facing changes.", Modifier.padding(top = Spacing.xs))
        } else {
            ChangelogSections(version.sections, Modifier.padding(top = Spacing.xs))
        }
        AppAlertButton("Open release page", onOpenRelease, style = AppAlertButtonStyle.Text)
    }
}

/** "2 added · 1 fixed", or a note when the version has no listed changes. */
fun changeSummary(version: ChangelogVersion): String =
    if (version.sections.isEmpty()) {
        "No user-facing changes"
    } else {
        version.sections.joinToString(" · ") { "${it.items.size} ${it.title.lowercase()}" }
    }
