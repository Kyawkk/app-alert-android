package com.kyawzinlinn.app.alert.feature.changelog.presentation

import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogSection
import com.kyawzinlinn.app.alert.feature.changelog.domain.ChangelogVersion

/** Sample data for previews only. */
internal object ChangelogPreviewData {
    const val INSTALLED = "1.0.17"

    val rich = ChangelogVersion(
        "1.0.19", "2026-11-12",
        listOf(
            ChangelogSection("Added", listOf("update: check and install updates from GitHub Releases")),
            ChangelogSection("Fixed", listOf("build: stop repackaging classes", "plain entry")),
            ChangelogSection("Performance", listOf("ui: smoother list scrolling")),
            ChangelogSection("Changed", listOf("settings: group update and changelog cards")),
        ),
    )

    val empty = ChangelogVersion("1.0.18", "2026-11-11", emptyList())

    val longEntry = ChangelogVersion(
        "1.0.16", "2026-11-11",
        listOf(
            ChangelogSection(
                "Fixed",
                listOf(
                    "alarm: raise the alarm stream to its maximum while ringing and restore it when the alarm " +
                        "stops, also after the process was killed in the middle of an alarm",
                ),
            ),
        ),
    )

    /** Enough versions to show the "Show older versions" button on a phone. */
    val versions: List<ChangelogVersion> = listOf(rich, empty, longEntry) + (15 downTo 10).map { patch ->
        ChangelogVersion(
            "1.0.$patch", "2026-11-10",
            if (patch % 2 == 0) {
                listOf(ChangelogSection("Added", listOf("site: add landing page")))
            } else {
                emptyList()
            },
        )
    }
}
