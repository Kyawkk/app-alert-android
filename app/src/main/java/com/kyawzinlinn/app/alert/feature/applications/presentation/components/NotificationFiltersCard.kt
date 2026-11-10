package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import android.app.Notification
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import com.kyawzinlinn.app.alert.feature.applications.presentation.AppRuleDetailAction

private val CategoryOptions = listOf(
    Notification.CATEGORY_MESSAGE to "Messages",
    Notification.CATEGORY_CALL to "Calls",
    Notification.CATEGORY_EMAIL to "Email",
    Notification.CATEGORY_SOCIAL to "Social",
    Notification.CATEGORY_REMINDER to "Reminders",
)

private val ScopeOptions = listOf(
    KeywordScope.TITLE_OR_CONTENT to "Title or text",
    KeywordScope.TITLE to "Title only",
    KeywordScope.CONTENT to "Text only",
)

/** Optional rules. With no keywords and no categories every notification of the app rings. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationFiltersCard(
    keywords: String,
    scope: KeywordScope,
    categories: Set<String>,
    changed: Boolean,
    onAction: (AppRuleDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAlertCard(modifier = modifier) {
        Text("Filters", style = MaterialTheme.typography.titleMedium)
        Text(
            "Leave empty to be alerted about every notification from this app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = keywords,
            onValueChange = { onAction(AppRuleDetailAction.KeywordsChanged(it)) },
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            label = { Text("Keywords (comma separated)") },
            supportingText = { Text("Alarm only if one of them appears") },
        )
        FlowRow(
            modifier = Modifier.padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            ScopeOptions.forEach { (option, label) ->
                FilterChip(
                    selected = scope == option,
                    onClick = { onAction(AppRuleDetailAction.ScopeChanged(option)) },
                    label = { Text(label) },
                )
            }
        }
        Text(
            "Notification types",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            CategoryOptions.forEach { (value, label) ->
                FilterChip(
                    selected = value in categories,
                    onClick = { onAction(AppRuleDetailAction.CategoryToggled(value)) },
                    label = { Text(label) },
                )
            }
        }
        if (changed) {
            AppAlertButton(
                "Save filters",
                { onAction(AppRuleDetailAction.SaveFilters) },
                Modifier.fillMaxWidth().padding(top = Spacing.sm),
            )
        }
    }
}

@AppAlertPreviews
@Composable
private fun NotificationFiltersCardPreview() {
    AppAlertTheme {
        NotificationFiltersCard(
            "urgent, call", KeywordScope.TITLE_OR_CONTENT, setOf(Notification.CATEGORY_MESSAGE), true, {},
            Modifier.padding(Spacing.md),
        )
    }
}
