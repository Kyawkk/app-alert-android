package com.kyawzinlinn.app.alert.feature.changelog.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChangelogParserTest {

    private val text = """
        # Changelog

        Generated from commit messages.

        ## Unreleased

        ### Added

        - **update:** not shown yet

        ## 1.0.17 - 2026-11-11

        ### Fixed

        - **build:** stop repackaging classes
        - plain entry

        ## 1.0.18 - 2026-11-12

        No user-facing changes.
    """.trimIndent()

    @Test
    fun skipsUnreleasedAndKeepsOrder() {
        assertEquals(listOf("1.0.17", "1.0.18"), ChangelogParser.parse(text).map { it.version })
    }

    @Test
    fun readsDateSectionsAndStripsBold() {
        val version = ChangelogParser.parse(text).first()
        assertEquals("2026-11-11", version.date)
        assertEquals(
            listOf(ChangelogSection("Fixed", listOf("build: stop repackaging classes", "plain entry"))),
            version.sections,
        )
    }

    @Test
    fun versionWithoutChangesHasNoSections() {
        assertEquals(emptyList<ChangelogSection>(), ChangelogParser.parse(text).last().sections)
    }

    @Test
    fun releaseNotesBodyIsParsedIntoSections() {
        val sections = ChangelogParser.parseSections("### Added\n\n- **site:** redesign\n\n### Fixed\n\n- thing")
        assertEquals(listOf("Added", "Fixed"), sections.map { it.title })
    }

    @Test
    fun githubGeneratedNotesYieldNothing() {
        assertEquals(emptyList<ChangelogSection>(), ChangelogParser.parseSections("## What's Changed\n* fix: x by @a in #1"))
    }

    @Test
    fun emptyTextHasNoVersions() {
        assertEquals(emptyList<ChangelogVersion>(), ChangelogParser.parse(""))
        assertNull(ChangelogParser.parse("# Changelog").firstOrNull())
    }
}
