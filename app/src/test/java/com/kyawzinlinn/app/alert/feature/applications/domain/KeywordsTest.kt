package com.kyawzinlinn.app.alert.feature.applications.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordsTest {

    @Test
    fun parseSplitsOnCommasAndLineBreaks() {
        assertEquals(listOf("urgent", "call me", "help"), Keywords.parse(" urgent, call me\nhelp ,, "))
    }

    @Test
    fun parseOfBlankTextIsEmpty() {
        assertEquals(emptyList<String>(), Keywords.parse("  , \n "))
    }

    @Test
    fun addAppendsNewKeywordsInOrder() {
        assertEquals(listOf("a", "b", "c"), Keywords.add(listOf("a"), "b, c"))
    }

    @Test
    fun addIgnoresDuplicatesIgnoringCase() {
        assertEquals(listOf("Urgent"), Keywords.add(listOf("Urgent"), "urgent, URGENT"))
    }

    @Test
    fun addStopsAtTheLimit() {
        val full = (1..Keywords.MAX_COUNT).map { "k$it" }
        assertEquals(full, Keywords.add(full, "one more"))
        assertEquals(Keywords.MAX_COUNT, Keywords.add(full.dropLast(2), "x, y, z").size)
    }

    @Test
    fun addShortensVeryLongKeywords() {
        val result = Keywords.add(emptyList(), "x".repeat(200))
        assertEquals(Keywords.MAX_LENGTH, result.single().length)
    }
}
