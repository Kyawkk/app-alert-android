package com.kyawzinlinn.app.alert.feature.applications.domain

/** Rules for the keyword list of one app. A keyword is matched ignoring case, so duplicates differ only by case. */
object Keywords {
    const val MAX_COUNT = 20
    const val MAX_LENGTH = 60

    /** Splits typed or pasted text at commas and line breaks into trimmed, non-empty keywords. */
    fun parse(text: String): List<String> =
        text.split(',', '\n').map { it.trim().take(MAX_LENGTH) }.filter { it.isNotEmpty() }

    /** Adds every keyword found in [text] that is not already present, up to [MAX_COUNT]. */
    fun add(current: List<String>, text: String): List<String> {
        var result = current
        for (word in parse(text)) {
            if (result.size >= MAX_COUNT) break
            if (result.none { it.equals(word, ignoreCase = true) }) result = result + word
        }
        return result
    }
}
