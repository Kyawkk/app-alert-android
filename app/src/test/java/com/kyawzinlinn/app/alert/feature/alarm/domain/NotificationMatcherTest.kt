package com.kyawzinlinn.app.alert.feature.alarm.domain

import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import com.kyawzinlinn.app.alert.testing.event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMatcherTest {
    private val base = AppRule("com.viber.voip")

    @Test fun `rule without filters matches everything`() {
        assertTrue(NotificationMatcher.matches(base, event()))
    }

    @Test fun `keyword is case insensitive and matches title or content by default`() {
        val rule = base.copy(keywords = listOf("URGENT"))
        assertTrue(NotificationMatcher.matches(rule, event(text = "this is urgent!")))
        assertTrue(NotificationMatcher.matches(rule, event(title = "Urgent: server", text = null)))
        assertFalse(NotificationMatcher.matches(rule, event(title = "Hi", text = "lunch?")))
    }

    @Test fun `any keyword is enough`() {
        val rule = base.copy(keywords = listOf("alpha", "beta"))
        assertTrue(NotificationMatcher.matches(rule, event(text = "got beta")))
    }

    @Test fun `title only scope ignores content`() {
        val rule = base.copy(keywords = listOf("boss"), keywordScope = KeywordScope.TITLE)
        assertFalse(NotificationMatcher.matches(rule, event(title = "Alice", text = "boss is here")))
        assertTrue(NotificationMatcher.matches(rule, event(title = "My boss", text = "x")))
    }

    @Test fun `content only scope ignores title`() {
        val rule = base.copy(keywords = listOf("boss"), keywordScope = KeywordScope.CONTENT)
        assertFalse(NotificationMatcher.matches(rule, event(title = "boss", text = "hello")))
        assertTrue(NotificationMatcher.matches(rule, event(title = "x", text = "the boss")))
    }

    @Test fun `category filter restricts notification types`() {
        val rule = base.copy(categories = setOf("call"))
        assertTrue(NotificationMatcher.matches(rule, event(category = "call")))
        assertFalse(NotificationMatcher.matches(rule, event(category = "msg")))
        assertFalse(NotificationMatcher.matches(rule, event(category = null)))
    }

    @Test fun `keywords and categories must both match`() {
        val rule = base.copy(keywords = listOf("hi"), categories = setOf("msg"))
        assertFalse(NotificationMatcher.matches(rule, event(text = "hi", category = "call")))
        assertTrue(NotificationMatcher.matches(rule, event(text = "hi", category = "msg")))
    }

    @Test fun `event toString never contains message content`() {
        val text = event(title = "secret-title", text = "secret-body").toString()
        assertFalse(text.contains("secret"))
    }
}
