package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import android.app.Notification
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFieldLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFilterChip
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.size
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
        AppAlertCardHeader("Filters")
        AppAlertSupportingText(
            "Leave empty to be alerted about every notification from this app.",
        )
        AppAlertTextField(
            value = keywords,
            onValueChange = { onAction(AppRuleDetailAction.KeywordsChanged(it)) },
            modifier = Modifier.padding(top = Spacing.sm),
            label = "Keywords (comma separated)",
            supportingText = "Alarm only if one of them appears",
            singleLine = false,
        )
        val keywordList = remember(keywords) {
            keywords.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        }
        if (keywordList.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                keywordList.forEach { word ->
                    InputChip(
                        selected = false,
                        onClick = {
                            onAction(AppRuleDetailAction.KeywordsChanged((keywordList - word).joinToString(", ")))
                        },
                        label = { Text(word) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove $word",
                                modifier = Modifier.size(InputChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }
        }
        FlowRow(
            modifier = Modifier.padding(top = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            ScopeOptions.forEach { (option, label) ->
                AppAlertFilterChip(
                    selected = scope == option,
                    onClick = { onAction(AppRuleDetailAction.ScopeChanged(option)) },
                    label = label,
                )
            }
        }
        AppAlertFieldLabel("Notification types", Modifier.padding(top = Spacing.sm))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            CategoryOptions.forEach { (value, label) ->
                AppAlertFilterChip(
                    selected = value in categories,
                    onClick = { onAction(AppRuleDetailAction.CategoryToggled(value)) },
                    label = label,
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
