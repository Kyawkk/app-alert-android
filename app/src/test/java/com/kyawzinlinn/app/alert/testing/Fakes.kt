package com.kyawzinlinn.app.alert.testing

import com.kyawzinlinn.app.alert.core.common.TimeProvider
import com.kyawzinlinn.app.alert.core.permissions.PermissionState
import com.kyawzinlinn.app.alert.core.permissions.PermissionStatusSource
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmPlayer
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmRequest
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettings
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettingsRepository
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmStartResult
import com.kyawzinlinn.app.alert.feature.alarm.domain.NotificationEvent
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRule
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRulesRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertOutcome
import com.kyawzinlinn.app.alert.feature.history.domain.AlertRecord
import com.kyawzinlinn.app.alert.feature.settings.domain.AppSettings
import com.kyawzinlinn.app.alert.feature.settings.domain.QuietHours
import com.kyawzinlinn.app.alert.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeTimeProvider(
    var now: Long = 1_000_000L,
    var dayOfWeek: Int = 1,
    var minuteOfDay: Int = 12 * 60,
) : TimeProvider {
    override fun nowMillis() = now
    override fun currentDayOfWeek() = dayOfWeek
    override fun currentMinuteOfDay() = minuteOfDay
}

class FakeAppRulesRepository(rules: List<AppRule> = emptyList()) : AppRulesRepository {
    val rules = MutableStateFlow(rules.associateBy { it.packageName })
    override fun observeRules(): Flow<List<AppRule>> = rules.map { it.values.toList() }
    override suspend fun getRule(packageName: String) = rules.value[packageName]
    override fun isSelectedOrNull(packageName: String) = rules.value.containsKey(packageName)
    override suspend fun upsert(rule: AppRule) { rules.value = rules.value + (rule.packageName to rule) }
    override suspend fun update(packageName: String, transform: (AppRule) -> AppRule) {
        rules.value[packageName]?.let { rules.value = rules.value + (packageName to transform(it)) }
    }
    override suspend fun remove(packageName: String) { rules.value = rules.value - packageName }
}

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    val settings = MutableStateFlow(initial)
    override fun observe(): Flow<AppSettings> = settings
    override suspend fun setMonitoringEnabled(enabled: Boolean) { settings.value = settings.value.copy(monitoringEnabled = enabled) }
    override suspend fun setQuietHours(quietHours: QuietHours) { settings.value = settings.value.copy(quietHours = quietHours) }
    override suspend fun updateQuietHours(transform: (QuietHours) -> QuietHours) {
        settings.value = settings.value.copy(quietHours = transform(settings.value.quietHours))
    }
    override suspend fun setOnboardingCompleted(completed: Boolean) { settings.value = settings.value.copy(onboardingCompleted = completed) }
}

class FakeAlarmSettingsRepository(initial: AlarmSettings = AlarmSettings()) : AlarmSettingsRepository {
    val settings = MutableStateFlow(initial)
    override fun observe(): Flow<AlarmSettings> = settings
    override suspend fun update(transform: (AlarmSettings) -> AlarmSettings) { settings.value = transform(settings.value) }
}

class FakeAlarmPlayer(var result: AlarmStartResult = AlarmStartResult.Started) : AlarmPlayer {
    val requests = mutableListOf<AlarmRequest>()
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    override suspend fun start(request: AlarmRequest): AlarmStartResult {
        requests += request
        _isPlaying.value = result == AlarmStartResult.Started
        return result
    }
    override fun stop() { _isPlaying.value = false }
}

class FakeHistoryRepository : AlertHistoryRepository {
    var failOnRecord = false
    val records = mutableListOf<Pair<String, AlertOutcome>>()
    override fun observeRecent(limit: Int): Flow<List<AlertRecord>> =
        MutableStateFlow(records.mapIndexed { i, (pkg, outcome) -> AlertRecord(i.toLong(), pkg, 0L, outcome) })
    override suspend fun record(packageName: String, outcome: AlertOutcome, timestampMillis: Long) {
        if (failOnRecord) throw java.io.IOException("disk full")
        records += packageName to outcome
    }
    override suspend fun clear() = records.clear()
}

class FakePermissionStatusSource(initial: PermissionState = PermissionState()) : PermissionStatusSource {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<PermissionState> = _state.asStateFlow()
    override fun refresh() = Unit
    fun set(state: PermissionState) { _state.value = state }
}

fun event(
    key: String = "key-1",
    pkg: String = "com.viber.voip",
    title: String? = "Alice",
    text: String? = "Hello there",
    category: String? = "msg",
    time: Long = 100L,
    ongoing: Boolean = false,
    summary: Boolean = false,
) = NotificationEvent(key, pkg, title, text, category, time, ongoing, summary)
