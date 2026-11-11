package com.kyawzinlinn.app.alert.feature.changelog.domain

data class ChangelogSection(val title: String, val items: List<String>)

data class ChangelogVersion(val version: String, val date: String?, val sections: List<ChangelogSection>)

/** Reads the markdown written by scripts/changelog.py and the release notes built from the same commits. */
object ChangelogParser {

    /** One entry per "## <version> - <date>" heading. The Unreleased block is not shown in the app. */
    fun parse(text: String): List<ChangelogVersion> {
        val versions = mutableListOf<ChangelogVersion>()
        var heading: String? = null
        val body = StringBuilder()

        fun flush() {
            val current = heading ?: return
            val title = current.substringBefore(" - ").trim()
            if (!title.equals("Unreleased", ignoreCase = true)) {
                val date = current.substringAfter(" - ", "").trim().ifEmpty { null }
                versions += ChangelogVersion(title, date, parseSections(body.toString()))
            }
        }

        for (line in text.lines()) {
            if (line.startsWith("## ")) {
                flush()
                heading = line.removePrefix("## ")
                body.clear()
            } else {
                body.appendLine(line)
            }
        }
        flush()
        return versions
    }

    /** "### Title" groups with "- item" lines. Anything else, such as free text, is ignored. */
    fun parseSections(text: String): List<ChangelogSection> {
        val sections = mutableListOf<ChangelogSection>()
        var title: String? = null
        var items = mutableListOf<String>()

        fun flush() {
            val current = title
            if (current != null && items.isNotEmpty()) sections += ChangelogSection(current, items)
        }

        for (line in text.lines()) {
            when {
                line.startsWith("### ") -> {
                    flush()
                    title = line.removePrefix("### ").trim()
                    items = mutableListOf()
                }
                line.startsWith("- ") && title != null -> items += line.removePrefix("- ").replace("**", "").trim()
            }
        }
        flush()
        return sections
    }
}

interface ChangelogRepository {
    /** The bundled changelog text, or null when it cannot be read. */
    suspend fun read(): String?
}

class GetChangelogUseCase(private val repository: ChangelogRepository) {
    suspend operator fun invoke(): List<ChangelogVersion> =
        repository.read()?.let(ChangelogParser::parse).orEmpty()
}
