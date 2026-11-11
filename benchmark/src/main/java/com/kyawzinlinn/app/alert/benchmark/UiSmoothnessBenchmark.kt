package com.kyawzinlinn.app.alert.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame timing while scrolling the Applications list and while switching tabs
 * (which also exercises the bottom bar and the screen transitions).
 */
@RunWith(AndroidJUnit4::class)
class UiSmoothnessBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Before
    fun finishOnboarding() = Onboarding.ensureFinished()

    @Test
    fun scrollApplicationsList() = measureFrames(
        setup = { openTab("Apps") },
    ) {
        val list = device.wait(Until.findObject(By.scrollable(true)), BenchmarkConfig.UI_TIMEOUT_MS)
            ?: error("Applications list not found")
        list.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            list.fling(Direction.DOWN)
            device.waitForIdle()
        }
        repeat(3) {
            list.fling(Direction.UP)
            device.waitForIdle()
        }
    }

    @Test
    fun switchTabs() = measureFrames(setup = {}) {
        listOf("History", "Settings", "Apps", "Home").forEach { openTab(it) }
    }

    private fun measureFrames(
        setup: MacrobenchmarkScope.() -> Unit,
        block: MacrobenchmarkScope.() -> Unit,
    ) = rule.measureRepeated(
        packageName = BenchmarkConfig.TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.WARM,
        iterations = BenchmarkConfig.UI_ITERATIONS,
        setupBlock = {
            pressHome()
            startActivityAndWait(BenchmarkConfig.launchIntent())
            setup()
        },
        measureBlock = block,
    )

    private fun MacrobenchmarkScope.openTab(label: String) {
        val tab = device.wait(Until.findObject(By.text(label)), BenchmarkConfig.UI_TIMEOUT_MS)
            ?: error("Tab \"$label\" not found")
        tab.click()
        device.waitForIdle()
    }
}
