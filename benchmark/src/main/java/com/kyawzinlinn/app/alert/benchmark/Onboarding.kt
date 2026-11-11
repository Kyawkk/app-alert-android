package com.kyawzinlinn.app.alert.benchmark

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/**
 * Gets a freshly installed app past onboarding, so every benchmark starts on Home.
 * The connected-test task uninstalls the app after each run, so this runs before every class.
 */
internal object Onboarding {
    private const val MAX_STEPS = 12
    private const val STEP_TIMEOUT_MS = 2_000L

    fun ensureFinished() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // started from the shell, so it works even when the test app cannot see other packages
        device.executeShellCommand("am start -W -n ${BenchmarkConfig.TARGET_PACKAGE}/${BenchmarkConfig.MAIN_ACTIVITY}")
        device.wait(Until.hasObject(By.pkg(BenchmarkConfig.TARGET_PACKAGE).depth(0)), BenchmarkConfig.UI_TIMEOUT_MS)

        for (step in 0 until MAX_STEPS) {
            val finish = device.findObject(By.text("Start using App Alert"))
            if (finish != null) {
                finish.click()
                device.waitForIdle()
                break
            }
            val next = device.wait(Until.findObject(By.text("Continue")), STEP_TIMEOUT_MS) ?: break
            next.click()
            device.waitForIdle()
        }

        check(device.wait(Until.hasObject(By.text("History")), BenchmarkConfig.UI_TIMEOUT_MS)) {
            "Could not get past onboarding automatically"
        }
        device.pressHome()
    }
}
