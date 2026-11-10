package com.kyawzinlinn.app.alert.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsAction
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsPreviewData
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsScreen
import com.kyawzinlinn.app.alert.feature.applications.presentation.ApplicationsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationsScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun show(state: ApplicationsUiState, onAction: (ApplicationsAction) -> Unit = {}) {
        compose.setContent { AppAlertTheme { ApplicationsScreen(state, onAction, {}) } }
    }

    @Test fun showsAppsAndSelectedCount() {
        show(ApplicationsPreviewData.normal)
        compose.onNodeWithText("Viber").assertIsDisplayed()
        compose.onNodeWithText("Telegram").assertIsDisplayed()
        compose.onNodeWithText("1 of 3 apps monitored").assertIsDisplayed()
    }

    @Test fun typingInSearchReportsTheQuery() {
        var action: ApplicationsAction? = null
        show(ApplicationsPreviewData.normal) { action = it }
        compose.onNode(hasSetTextAction()).performTextInput("vib")
        assertEquals(ApplicationsAction.QueryChanged("vib"), action)
    }

    @Test fun togglingAnAppReportsTheSelection() {
        var action: ApplicationsAction? = null
        show(ApplicationsPreviewData.normal) { action = it }
        compose.onNodeWithText("Telegram").performClick()
        assertEquals(ApplicationsAction.SetSelected("org.telegram.messenger", true), action)
    }

    @Test fun emptySearchResultExplainsWhatHappened() {
        show(ApplicationsPreviewData.noResults)
        compose.onNodeWithText("No matching apps").assertIsDisplayed()
    }

    @Test fun failureOffersRetry() {
        var action: ApplicationsAction? = null
        show(ApplicationsUiState(isLoading = false, loadFailed = true)) { action = it }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(ApplicationsAction.Retry, action)
    }

    @Test fun clearSearchButtonAppearsWithText() {
        show(ApplicationsPreviewData.noResults)
        compose.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }
}
