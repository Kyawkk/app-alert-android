package com.kyawzinlinn.app.alert.feature

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kyawzinlinn.app.alert.core.designsystem.theme.AppAlertTheme
import com.kyawzinlinn.app.alert.core.ui.WindowWidth
import com.kyawzinlinn.app.alert.feature.dashboard.domain.MonitoringStatus
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardAction
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardPreviewData
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardScreen
import com.kyawzinlinn.app.alert.feature.dashboard.presentation.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun show(state: DashboardUiState, onAction: (DashboardAction) -> Unit = {}, onNotificationAccess: () -> Unit = {}) {
        compose.setContent {
            AppAlertTheme {
                DashboardScreen(state, WindowWidth.Compact, onAction, {}, {}, {}, {}, {}, onNotificationAccess)
            }
        }
    }

    @Test fun activeStateIsShownWithMonitoredApps() {
        show(DashboardPreviewData.normal)
        compose.onNodeWithText("Monitoring is on").assertIsDisplayed()
        compose.onNodeWithText("Viber").assertIsDisplayed()
        compose.onNodeWithText("Recent alerts").assertIsDisplayed()
    }

    @Test fun missingPermissionIsExplainedAndOffersTheSettingsAction() {
        var opened = false
        show(
            DashboardPreviewData.normal.copy(monitoringStatus = MonitoringStatus.NOTIFICATION_ACCESS_MISSING),
            onNotificationAccess = { opened = true },
        )
        compose.onNodeWithText("Notification access is off").assertIsDisplayed()
        compose.onNodeWithText("Open notification access").performClick()
        assertEquals(true, opened)
    }

    @Test fun emptyStateInvitesTheUserToChooseApps() {
        show(DashboardPreviewData.empty)
        compose.onNodeWithText("Nothing monitored yet").assertIsDisplayed()
    }

    @Test fun monitoringSwitchReportsTheNewValue() {
        var action: DashboardAction? = null
        show(DashboardPreviewData.normal, onAction = { action = it })
        compose.onNodeWithText("Monitor notifications").performClick()
        assertEquals(DashboardAction.SetMonitoringEnabled(false), action)
    }

    @Test fun testAlarmButtonTriggersTheAction() {
        var action: DashboardAction? = null
        show(DashboardPreviewData.normal, onAction = { action = it })
        compose.onNodeWithText("Test alarm").performScrollTo().performClick()
        assertEquals(DashboardAction.TestAlarm, action)
    }
}
