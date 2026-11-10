package com.kyawzinlinn.app.alert.feature.settings.domain

import com.kyawzinlinn.app.alert.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateQuietHoursUseCaseTest {
    private val repository = FakeSettingsRepository()
    private val update = UpdateQuietHoursUseCase(repository)

    @Test fun `values are clamped into valid ranges`() = runTest {
        update { it.copy(startMinuteOfDay = 99_999, endMinuteOfDay = -4, days = setOf(0, 3, 9)) }
        val stored = repository.settings.value.quietHours
        assertEquals(24 * 60 - 1, stored.startMinuteOfDay)
        assertEquals(0, stored.endMinuteOfDay)
        assertEquals(setOf(3), stored.days)
    }

    @Test fun `successive updates build on each other`() = runTest {
        update { it.copy(enabled = true) }
        update { it.copy(days = setOf(1)) }
        val stored = repository.settings.value.quietHours
        assertEquals(true, stored.enabled)
        assertEquals(setOf(1), stored.days)
    }
}
