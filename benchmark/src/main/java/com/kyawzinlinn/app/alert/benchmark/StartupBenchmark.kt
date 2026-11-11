package com.kyawzinlinn.app.alert.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup time with no ahead-of-time compilation (worst case, like a fresh install)
 * and with full compilation (best case, like after background dexopt).
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun finishOnboarding() = Onboarding.ensureFinished()

    @Test fun coldWithoutCompilation() = startup(StartupMode.COLD, CompilationMode.None())

    @Test fun coldFullyCompiled() = startup(StartupMode.COLD, CompilationMode.Full())

    @Test fun warm() = startup(StartupMode.WARM, CompilationMode.Full())

    private fun startup(mode: StartupMode, compilation: CompilationMode) = rule.measureRepeated(
        packageName = BenchmarkConfig.TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilation,
        startupMode = mode,
        iterations = BenchmarkConfig.STARTUP_ITERATIONS,
    ) {
        pressHome()
        startActivityAndWait(BenchmarkConfig.launchIntent())
    }
}
