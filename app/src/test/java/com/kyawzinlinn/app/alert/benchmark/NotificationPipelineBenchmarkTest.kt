package com.kyawzinlinn.app.alert.benchmark

import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlertThrottle
import com.kyawzinlinn.app.alert.feature.alarm.domain.ProcessIncomingNotificationUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ShouldTriggerAlarmUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StartAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.testing.FakeAlarmPlayer
import com.kyawzinlinn.app.alert.testing.FakeAlarmSettingsRepository
import com.kyawzinlinn.app.alert.testing.FakeAppRulesRepository
import com.kyawzinlinn.app.alert.testing.FakeHistoryRepository
import com.kyawzinlinn.app.alert.testing.FakeSettingsRepository
import com.kyawzinlinn.app.alert.testing.FakeTimeProvider
import com.kyawzinlinn.app.alert.testing.event
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Cost of the notification pipeline (rule lookup, filters, throttle, alarm start, history write)
 * with in-memory fakes, so it isolates our own logic from Room, DataStore and audio.
 * It does not replace measuring real latency on a device.
 *
 * Skipped unless RUN_BENCHMARKS=1:
 *   RUN_BENCHMARKS=1 ./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest \
 *       --tests '*NotificationPipelineBenchmarkTest'
 */
class NotificationPipelineBenchmarkTest {

    @Test
    fun measurePipeline() {
        assumeTrue("Set RUN_BENCHMARKS=1 to run", System.getenv("RUN_BENCHMARKS") == "1")

        val results = linkedMapOf<String, Map<String, Double>>()
        results["triggered"] = measure(selectedApp = true)
        results["ignored_unselected_app"] = measure(selectedApp = false)

        val json = results.entries.joinToString(",\n", "{\n", "\n}\n") { (name, stats) ->
            "  \"$name\": {" + stats.entries.joinToString(", ") { "\"${it.key}\": ${"%.1f".format(it.value)}" } + "}"
        }
        val out = File("build/benchmark-results").apply { mkdirs() }.resolve("notification-pipeline.json")
        out.writeText(json)

        println("Notification pipeline (microseconds per event):")
        results.forEach { (name, s) ->
            println("  %-24s p50=%.1f  p95=%.1f  p99=%.1f  max=%.1f".format(name, s.getValue("p50_us"), s.getValue("p95_us"), s.getValue("p99_us"), s.getValue("max_us")))
        }
    }

    private fun measure(selectedApp: Boolean): Map<String, Double> = runBlocking {
        val time = FakeTimeProvider()
        val alarmSettings = FakeAlarmSettingsRepository(AlarmSettings())
        val player = FakeAlarmPlayer()
        val useCase = ProcessIncomingNotificationUseCase(
            appRules = FakeAppRulesRepository(listOf(AppRule(PACKAGE))),
            settings = FakeSettingsRepository(),
            alarmSettings = alarmSettings,
            shouldTrigger = ShouldTriggerAlarmUseCase(AlertThrottle(), time),
            startAlarm = StartAlarmPlaybackUseCase(alarmSettings, player),
            history = FakeHistoryRepository(),
            timeProvider = time,
        )
        val pkg = if (selectedApp) PACKAGE else "com.other.app"

        fun fire(i: Int) {
            // moving the clock past any cooldown keeps every event on the same code path
            time.now += 3_600_000L
            runBlocking { useCase(event(key = "k$i", pkg = pkg, text = "message $i", time = time.now)) }
        }

        repeat(WARMUP) { fire(it) }
        val samples = LongArray(RUNS)
        for (i in 0 until RUNS) {
            val start = System.nanoTime()
            fire(WARMUP + i)
            samples[i] = System.nanoTime() - start
        }
        samples.sort()
        fun pct(p: Double) = samples[((samples.size - 1) * p).toInt()] / 1_000.0
        mapOf("p50_us" to pct(0.50), "p95_us" to pct(0.95), "p99_us" to pct(0.99), "max_us" to samples.last() / 1_000.0)
    }

    private companion object {
        const val PACKAGE = "com.viber.voip"
        const val WARMUP = 5_000
        const val RUNS = 50_000
    }
}
