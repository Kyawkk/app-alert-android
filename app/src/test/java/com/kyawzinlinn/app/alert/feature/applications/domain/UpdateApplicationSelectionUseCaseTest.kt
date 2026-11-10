package com.kyawzinlinn.app.alert.feature.applications.domain

import com.kyawzinlinn.app.alert.testing.FakeAppRulesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateApplicationSelectionUseCaseTest {
    private val repository = FakeAppRulesRepository()
    private val select = UpdateApplicationSelectionUseCase(repository)

    @Test fun `selecting creates an enabled rule`() = runTest {
        select("com.viber.voip", true)
        assertTrue(repository.getRule("com.viber.voip")!!.enabled)
    }

    @Test fun `deselecting removes the rule`() = runTest {
        select("com.viber.voip", true)
        select("com.viber.voip", false)
        assertNull(repository.getRule("com.viber.voip"))
    }

    @Test fun `re-selecting keeps an existing configuration`() = runTest {
        repository.upsert(AppRule("a", enabled = false, keywords = listOf("x")))
        select("a", true)
        val rule = repository.getRule("a")!!
        assertTrue(rule.enabled)
        assertEquals(listOf("x"), rule.keywords)
    }

    @Test fun `rule update trims and de-duplicates keywords`() = runTest {
        repository.upsert(AppRule("a"))
        UpdateAppRuleUseCase(repository)("a") { it.copy(keywords = listOf(" hi ", "", "hi", "bye")) }
        assertEquals(listOf("hi", "bye"), repository.getRule("a")!!.keywords)
    }

    @Test fun `updating an unselected app does nothing`() = runTest {
        UpdateAppRuleUseCase(repository)("missing") { it.copy(enabled = false) }
        assertNull(repository.getRule("missing"))
    }
}
