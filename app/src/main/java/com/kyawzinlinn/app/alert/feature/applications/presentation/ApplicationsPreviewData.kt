package com.kyawzinlinn.app.alert.feature.applications.presentation

import com.kyawzinlinn.app.alert.core.designsystem.preview.PreviewText

object ApplicationsPreviewData {
    val normal = ApplicationsUiState(
        isLoading = false,
        apps = listOf(
            ApplicationItemUiModel("com.viber.voip", "Viber", true),
            ApplicationItemUiModel("org.telegram.messenger", "Telegram", false),
            ApplicationItemUiModel("long.name", PreviewText.LONG_APP_NAME, false),
        ),
        selectedCount = 1,
        totalCount = 3,
    )

    val noResults = ApplicationsUiState(isLoading = false, query = "zzz", selectedCount = 1, totalCount = 3)
}
