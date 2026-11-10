package com.kyawzinlinn.app.alert

import android.app.Application
import com.kyawzinlinn.app.alert.app.AppContainer

class AppAlertApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Warm the in-memory rule snapshot so the listener can decide immediately after process start.
        container.appRulesRepository
    }
}
