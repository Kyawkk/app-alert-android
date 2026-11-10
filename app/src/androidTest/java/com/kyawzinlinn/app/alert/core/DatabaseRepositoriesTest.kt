package com.kyawzinlinn.app.alert.core

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kyawzinlinn.app.alert.core.database.AppAlertDatabase
import com.kyawzinlinn.app.alert.feature.applications.data.RoomAppRulesRepository
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.KeywordScope
import com.kyawzinlinn.app.alert.feature.history.data.RoomAlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DatabaseRepositoriesTest {
    private lateinit var db: AppAlertDatabase
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppAlertDatabase::class.java).build()
    }

    @After fun tearDown() = db.close()

    @Test fun historyIsReturnedNewestFirst() = runBlocking {
        val repository = RoomAlertHistoryRepository(db.alertHistoryDao())
        repository.record("a", AlertOutcome.TRIGGERED, 1_000)
        repository.record("b", AlertOutcome.SUPPRESSED_COOLDOWN, 2_000)
        val recent = repository.observeRecent(10).first()
        assertEquals(listOf("b", "a"), recent.map { it.packageName })
        assertEquals(AlertOutcome.SUPPRESSED_COOLDOWN, recent.first().outcome)
    }

    @Test fun historyOlderThanRetentionIsPruned() = runBlocking {
        val repository = RoomAlertHistoryRepository(db.alertHistoryDao())
        val now = TimeUnit.DAYS.toMillis(100)
        repository.record("old", AlertOutcome.TRIGGERED, now - TimeUnit.DAYS.toMillis(31))
        repository.record("new", AlertOutcome.TRIGGERED, now)
        assertEquals(listOf("new"), repository.observeRecent(10).first().map { it.packageName })
    }

    @Test fun historyNeverExceedsMaxEntries() = runBlocking {
        val repository = RoomAlertHistoryRepository(db.alertHistoryDao())
        val base = TimeUnit.DAYS.toMillis(100)
        repeat(AlertHistoryRepository.MAX_ENTRIES + 25) { repository.record("p", AlertOutcome.TRIGGERED, base + it) }
        assertEquals(AlertHistoryRepository.MAX_ENTRIES, repository.observeRecent(10_000).first().size)
    }

    @Test fun clearRemovesEverything() = runBlocking {
        val repository = RoomAlertHistoryRepository(db.alertHistoryDao())
        repository.record("a", AlertOutcome.TRIGGERED, 1)
        repository.clear()
        assertTrue(repository.observeRecent(10).first().isEmpty())
    }

    @Test fun appRulesRoundTripAndSnapshotIsKeptInSync() = runBlocking {
        val repository = RoomAppRulesRepository(db.appRuleDao(), scope)
        val rule = AppRule(
            "com.viber.voip", enabled = false, soundUri = "content://sound",
            keywords = listOf("urgent", "call me"), keywordScope = KeywordScope.TITLE, categories = setOf("msg", "call"),
        )
        repository.upsert(rule)
        assertEquals(rule, repository.getRule("com.viber.voip"))

        repository.remove("com.viber.voip")
        assertNull(repository.getRule("com.viber.voip"))
        assertFalse(repository.observeRules().first().any { it.packageName == "com.viber.voip" })
    }
}
