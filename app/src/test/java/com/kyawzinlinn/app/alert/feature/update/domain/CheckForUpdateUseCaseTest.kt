package com.kyawzinlinn.app.alert.feature.update.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckForUpdateUseCaseTest {

    private fun info(code: Long) = UpdateInfo(code, "1.0.$code", "https://example.com/a.apk", 1_000, "a".repeat(64), "https://example.com")

    private class FakeRepository(private val lookup: UpdateLookup) : UpdateRepository {
        override suspend fun fetchLatest() = lookup
        override suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): DownloadResult =
            DownloadResult.Done("/tmp/a.apk")
    }

    private fun check(lookup: UpdateLookup, installed: Long) =
        CheckForUpdateUseCase(FakeRepository(lookup)) { installed }

    @Test
    fun newerReleaseIsAvailable() = runTest {
        val result = check(UpdateLookup.Found(info(12)), installed = 11)()
        assertEquals(UpdateCheckResult.Available(info(12)), result)
    }

    @Test
    fun sameVersionIsUpToDate() = runTest {
        assertEquals(UpdateCheckResult.UpToDate, check(UpdateLookup.Found(info(11)), installed = 11)())
    }

    @Test
    fun olderReleaseIsNeverOffered() = runTest {
        assertEquals(UpdateCheckResult.UpToDate, check(UpdateLookup.Found(info(5)), installed = 11)())
    }

    @Test
    fun noReleaseIsUpToDate() = runTest {
        assertEquals(UpdateCheckResult.UpToDate, check(UpdateLookup.NoRelease, installed = 1)())
    }

    @Test
    fun failureIsPassedThrough() = runTest {
        val result = check(UpdateLookup.Failed(UpdateFailure.NO_NETWORK), installed = 1)()
        assertEquals(UpdateCheckResult.Failed(UpdateFailure.NO_NETWORK), result)
    }
}
