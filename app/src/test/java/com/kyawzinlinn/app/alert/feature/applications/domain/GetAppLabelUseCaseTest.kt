package com.kyawzinlinn.app.alert.feature.applications.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAppLabelUseCaseTest {

    private class CountingRepository : InstalledAppsRepository {
        val lookups = mutableListOf<String>()

        override suspend fun getInstalledApps(forceRefresh: Boolean): List<InstalledApp> = emptyList()

        override suspend fun labelOf(packageName: String): String {
            lookups += packageName
            return packageName.uppercase()
        }
    }

    @Test
    fun forAllLooksEachPackageUpOnce() = runTest {
        val repository = CountingRepository()
        val labels = GetAppLabelUseCase(repository).forAll(listOf("a", "b", "a", "a", "b"))
        assertEquals(mapOf("a" to "A", "b" to "B"), labels)
        assertEquals(listOf("a", "b"), repository.lookups)
    }

    @Test
    fun forAllOfNothingIsEmpty() = runTest {
        assertEquals(emptyMap<String, String>(), GetAppLabelUseCase(CountingRepository()).forAll(emptyList()))
    }
}
