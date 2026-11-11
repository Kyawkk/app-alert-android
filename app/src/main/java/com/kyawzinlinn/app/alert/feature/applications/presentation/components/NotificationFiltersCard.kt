package com.kyawzinlinn.app.alert.feature.applications.presentation.components

import android.app.Notification
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

private const val CHIP_FADE_MILLIS = 150
private const val CHIP_SCALE_MILLIS = 200
private const val CHIP_EXIT_MILLIS = 150
private const val CHIP_SHRINK_MILLIS = 180
private const val CHIP_START_SCALE = 0.8f
private const val BUTTON_MILLIS = 180

@Composable
fun NotificationFiltersCard(
    keywords: List<String>,
    scope: KeywordScope,
    categories: Set<String>,
    changed: Boolean,
    onAction: (AppRuleDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The typed text lives in this state object and is only read inside KeywordInputField, so
    // typing recomposes that field and not the chips and options below it.
    val input = rememberSaveable { mutableStateOf("") }
    val hasPending by remember { derivedStateOf { input.value.isNotBlank() } }

    AppAlertCard(modifier = modifier) {
        AppAlertCardHeader("Filters")
        AppAlertSupportingText(
            "Leave empty to be alerted about every notification from this app.",
        )
        AppAlertFieldLabel("Keywords", Modifier.padding(top = Spacing.sm))
        KeywordInputField(input, atLimit = keywords.size >= Keywords.MAX_COUNT, onAction = onAction)
        AppAlertSupportingText(
            "Alarm if any keyword appears. Press Done or type a comma to add another " +
                "(${keywords.size}/${Keywords.MAX_COUNT}).",
            Modifier.padding(top = Spacing.xs),
        )
        KeywordChips(keywords, onRemove = { onAction(AppRuleDetailAction.KeywordRemoved(it)) })
        ScopeAndCategoryOptions(scope, categories, onAction)
        AnimatedVisibility(
            visible = changed || hasPending,
            enter = fadeIn(tween(BUTTON_MILLIS)) + expandVertically(tween(BUTTON_MILLIS)),
            exit = fadeOut(tween(BUTTON_MILLIS)) + shrinkVertically(tween(BUTTON_MILLIS)),
        ) {
            AppAlertButton(
                "Save filters",
                {
                    onAction(AppRuleDetailAction.SaveFilters(input.value))
                    input.value = ""
                },
                Modifier.fillMaxWidth().padding(top = Spacing.sm),
            )
        }
    }
}

@Composable
private fun KeywordInputField(
    input: MutableState<String>,
    atLimit: Boolean,
    onAction: (AppRuleDetailAction) -> Unit,
) {
    fun commit() {
        if (input.value.isNotBlank()) onAction(AppRuleDetailAction.KeywordsAdded(input.value))
        input.value = ""
    }
    AppAlertTextField(
        value = input.value,
        onValueChange = { text ->
            if (',' in text || '\n' in text) {
                // a comma or a pasted list finishes the keywords before it
                val parts = text.split(',', '\n')
                onAction(AppRuleDetailAction.KeywordsAdded(parts.dropLast(1).joinToString(",")))
                input.value = parts.last().trimStart()
            } else {
                input.value = text
            }
        },
        modifier = Modifier.padding(top = Spacing.xs),
        placeholder = if (atLimit) "Keyword limit reached" else "Add a keyword",
        leadingIcon = Icons.Default.Add,
        trailingIcon = if (input.value.isNotBlank()) {
            {
                IconButton(onClick = ::commit) {
                    Icon(Icons.Default.Check, contentDescription = "Add keyword")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { commit() }),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChips(keywords: List<String>, onRemove: (String) -> Unit) {
    if (keywords.isEmpty()) return
    // chips that are already there when the card opens appear at once; only new ones animate in
    val initial = remember { keywords.toSet() }
    FlowRow(
        modifier = Modifier.padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        keywords.forEach { word ->
            key(word) { KeywordChip(word, animateIn = word !in initial, onRemoved = { onRemove(word) }) }
        }
    }
}

/** Fades and scales in. When tapped it animates out first and only then asks to be removed. */
@Composable
private fun KeywordChip(word: String, animateIn: Boolean, onRemoved: () -> Unit) {
    val visibility = remember { MutableTransitionState(!animateIn).apply { targetState = true } }
    LaunchedEffect(visibility.isIdle, visibility.currentState) {
        if (visibility.isIdle && !visibility.currentState) onRemoved()
    }
    AnimatedVisibility(
        visibleState = visibility,
        // fade and scale only touch the draw layer; the width shrink on exit lets the next chips glide over
        enter = fadeIn(tween(CHIP_FADE_MILLIS)) + scaleIn(tween(CHIP_SCALE_MILLIS), initialScale = CHIP_START_SCALE),
        exit = fadeOut(tween(CHIP_EXIT_MILLIS)) +
            scaleOut(tween(CHIP_EXIT_MILLIS), targetScale = CHIP_START_SCALE) +
            shrinkHorizontally(tween(CHIP_SHRINK_MILLIS)),
    ) {
        InputChip(
            selected = false,
            onClick = { visibility.targetState = false },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScopeAndCategoryOptions(
    scope: KeywordScope,
    categories: Set<String>,
    onAction: (AppRuleDetailAction) -> Unit,
) {
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
