package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import android.app.Notification
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertButton
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCard
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertCardHeader
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFieldLabel
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertFilterChip
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertSupportingText
import com.kyawzinlinn.app.alert.core.designsystem.components.AppAlertTextField
import com.kyawzinlinn.app.alert.core.designsystem.preview.AppAlertPreviews
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.designsystem.theme.Spacing
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import com.kyawzinlinn.app.alert.feature.applications.domain.Keywords
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
    keywords: List<String>,
    scope: KeywordScope,
    categories: Set<String>,
    changed: Boolean,
    onAction: (AppRuleDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // text still being typed; it is kept here so typing never waits for the view model
    var input by rememberSaveable { mutableStateOf("") }
    val atLimit = keywords.size >= Keywords.MAX_COUNT

    fun addInput() {
        if (input.isNotBlank()) onAction(AppRuleDetailAction.KeywordsAdded(input))
        input = ""
    }

    AppAlertCard(modifier = modifier) {
        AppAlertCardHeader("Filters")
        AppAlertSupportingText(
            "Leave empty to be alerted about every notification from this app.",
        )
        AppAlertFieldLabel("Keywords", Modifier.padding(top = Spacing.sm))
        AppAlertTextField(
            value = input,
            onValueChange = { text ->
                if (',' in text || '\n' in text) {
                    // a comma or pasted list finishes the keywords before it
                    val parts = text.split(',', '\n')
                    onAction(AppRuleDetailAction.KeywordsAdded(parts.dropLast(1).joinToString(",")))
                    input = parts.last().trimStart()
                } else {
                    input = text
                }
            },
            modifier = Modifier.padding(top = Spacing.xs),
            placeholder = if (atLimit) "Keyword limit reached" else "Add a keyword",
            leadingIcon = Icons.Default.Add,
            trailingIcon = if (input.isNotBlank()) {
                {
                    IconButton(onClick = ::addInput) {
                        Icon(Icons.Default.Check, contentDescription = "Add keyword")
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addInput() }),
        )
        AppAlertSupportingText(
            "Alarm if any keyword appears. Press Done or type a comma to add another " +
                "(${keywords.size}/${Keywords.MAX_COUNT}).",
            Modifier.padding(top = Spacing.xs),
        )
        if (keywords.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                keywords.forEach { word ->
                    key(word) {
                        InputChip(
                            selected = false,
                            onClick = { onAction(AppRuleDetailAction.KeywordRemoved(word)) },
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
        }
        FlowRow(
            modifier = Modifier.padding(top = Spacing.sm),
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
        if (changed || input.isNotBlank()) {
            AppAlertButton(
                "Save filters",
                {
                    onAction(AppRuleDetailAction.SaveFilters(input))
                    input = ""
                },
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
            listOf("urgent", "call", "invoice due"), KeywordScope.TITLE_OR_CONTENT,
            setOf(Notification.CATEGORY_MESSAGE), true, {}, Modifier.padding(Spacing.md),
        )
    }
}

@AppAlertPreviews
@Composable
private fun NotificationFiltersCardEmptyPreview() {
    AppAlertTheme {
        NotificationFiltersCard(
            emptyList(), KeywordScope.TITLE_OR_CONTENT, emptySet(), false, {}, Modifier.padding(Spacing.md),
        )
    }
}
