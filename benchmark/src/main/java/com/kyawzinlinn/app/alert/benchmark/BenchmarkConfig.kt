package com.kyawzinlinn.app.alert.benchmark

import android.content.ComponentName
import android.content.Intent

internal object BenchmarkConfig {
    const val TARGET_PACKAGE = "com.kyawzinlinn.app.alert"
    const val MAIN_ACTIVITY = "$TARGET_PACKAGE.MainActivity"
    const val STARTUP_ITERATIONS = 10
    const val UI_ITERATIONS = 5
    const val UI_TIMEOUT_MS = 5_000L

    /** Upper bound for waiting after a gesture; animated screens are rarely fully idle. */
    const val IDLE_TIMEOUT_MS = 1_000L

    /**
     * Explicit launch intent. Looking the app up by package name needs package visibility,
     * which some vendor ROMs hide behind a permission prompt.
     */
    fun launchIntent(): Intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(ComponentName(TARGET_PACKAGE, MAIN_ACTIVITY))
}
