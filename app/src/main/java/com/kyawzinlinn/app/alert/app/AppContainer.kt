package com.kyawzinlinn.app.alert.app

import android.content.Context
import com.kyawzinlinn.app.alert.core.audio.AlarmNotifier
import com.kyawzinlinn.app.alert.core.audio.AlarmPlaybackController
import com.kyawzinlinn.app.alert.core.audio.AndroidSoundInspector
import com.kyawzinlinn.app.alert.core.common.SystemTimeProvider
import com.kyawzinlinn.app.alert.core.common.TimeProvider
import com.kyawzinlinn.app.alert.core.database.AppAlertDatabase
import com.kyawzinlinn.app.alert.core.datastore.appAlertDataStore
import com.kyawzinlinn.app.alert.core.notifications.ListenerConnection
import com.kyawzinlinn.app.alert.core.permissions.AndroidPermissionStatusSource
import com.kyawzinlinn.app.alert.core.permissions.PermissionStatusSource
import com.kyawzinlinn.app.alert.core.ui.AppIconLoader
import com.kyawzinlinn.app.alert.feature.alarm.data.DataStoreAlarmSettingsRepository
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmPlayer
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmSettingsRepository
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlertThrottle
import com.kyawzinlinn.app.alert.feature.alarm.domain.GetSoundTitleUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmPlaybackStateUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ObserveAlarmSettingsUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ProcessIncomingNotificationUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.SelectAlarmSoundUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.ShouldTriggerAlarmUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.SoundInspector
import com.kyawzinlinn.app.alert.feature.alarm.domain.StartAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.StopAlarmPlaybackUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.UpdateAlarmSettingsUseCase
import com.kyawzinlinn.app.alert.feature.alarm.domain.VerifySoundUseCase
import com.kyawzinlinn.app.alert.feature.applications.data.PackageManagerInstalledAppsRepository
import com.kyawzinlinn.app.alert.feature.applications.data.RoomAppRulesRepository
import com.kyawzinlinn.app.alert.feature.applications.domain.AppRulesRepository
import com.kyawzinlinn.app.alert.feature.applications.domain.GetAppLabelUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.GetInstalledApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.InstalledAppsRepository
import com.kyawzinlinn.app.alert.feature.applications.domain.ObserveSelectedApplicationsUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.UpdateAppRuleUseCase
import com.kyawzinlinn.app.alert.feature.applications.domain.UpdateApplicationSelectionUseCase
import com.kyawzinlinn.app.alert.feature.dashboard.domain.ObserveMonitoringStatusUseCase
import com.kyawzinlinn.app.alert.feature.history.data.RoomAlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.AlertHistoryRepository
import com.kyawzinlinn.app.alert.feature.history.domain.ClearHistoryUseCase
import com.kyawzinlinn.app.alert.feature.history.domain.GetNotificationHistoryUseCase
import com.kyawzinlinn.app.alert.feature.settings.data.DataStoreSettingsRepository
import com.kyawzinlinn.app.alert.feature.settings.domain.CompleteOnboardingUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.ObserveAppSettingsUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.SetMonitoringEnabledUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.SetThemeModeUseCase
import com.kyawzinlinn.app.alert.feature.settings.domain.SettingsRepository
import com.kyawzinlinn.app.alert.feature.settings.domain.UpdateQuietHoursUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val timeProvider: TimeProvider = SystemTimeProvider()

    private val database: AppAlertDatabase by lazy { AppAlertDatabase.create(appContext) }
    val listenerConnection = ListenerConnection()
    val permissionStatusSource: PermissionStatusSource = AndroidPermissionStatusSource(appContext)
    val appIconLoader = AppIconLoader(appContext)

    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(appContext.appAlertDataStore)
    val alarmSettingsRepository: AlarmSettingsRepository =
        DataStoreAlarmSettingsRepository(appContext.appAlertDataStore)
    val appRulesRepository: AppRulesRepository by lazy {
        RoomAppRulesRepository(database.appRuleDao(), applicationScope)
    }
    val alertHistoryRepository: AlertHistoryRepository by lazy {
        RoomAlertHistoryRepository(database.alertHistoryDao())
    }
    val installedAppsRepository: InstalledAppsRepository = PackageManagerInstalledAppsRepository(appContext)
    val soundInspector: SoundInspector = AndroidSoundInspector(appContext)
    val alarmPlayer: AlarmPlayer =
        AlarmPlaybackController(appContext, applicationScope, AlarmNotifier(appContext))

    val observeAlarmSettings = ObserveAlarmSettingsUseCase(alarmSettingsRepository)
    val updateAlarmSettings = UpdateAlarmSettingsUseCase(alarmSettingsRepository)
    val startAlarmPlayback = StartAlarmPlaybackUseCase(alarmSettingsRepository, alarmPlayer)
    val stopAlarmPlayback = StopAlarmPlaybackUseCase(alarmPlayer)
    val selectAlarmSound = SelectAlarmSoundUseCase(soundInspector, updateAlarmSettings)
    val observeAlarmPlaybackState = ObserveAlarmPlaybackStateUseCase(alarmPlayer)
    val getSoundTitle = GetSoundTitleUseCase(soundInspector)
    val verifySound = VerifySoundUseCase(soundInspector)

    val getInstalledApplications = GetInstalledApplicationsUseCase(installedAppsRepository)
    val observeSelectedApplications by lazy { ObserveSelectedApplicationsUseCase(appRulesRepository) }
    val updateApplicationSelection by lazy { UpdateApplicationSelectionUseCase(appRulesRepository) }
    val updateAppRule by lazy { UpdateAppRuleUseCase(appRulesRepository) }
    val getAppLabel = GetAppLabelUseCase(installedAppsRepository)

    val observeAppSettings = ObserveAppSettingsUseCase(settingsRepository)
    val setMonitoringEnabled = SetMonitoringEnabledUseCase(settingsRepository)
    val updateQuietHours = UpdateQuietHoursUseCase(settingsRepository)
    val setThemeMode = SetThemeModeUseCase(settingsRepository)
    val completeOnboarding = CompleteOnboardingUseCase(settingsRepository)
    val getNotificationHistory by lazy { GetNotificationHistoryUseCase(alertHistoryRepository) }
    val clearHistory by lazy { ClearHistoryUseCase(alertHistoryRepository) }
    val observeMonitoringStatus by lazy {
        ObserveMonitoringStatusUseCase(
            settingsRepository, appRulesRepository, permissionStatusSource, listenerConnection, timeProvider,
        )
    }

    private val shouldTriggerAlarm = ShouldTriggerAlarmUseCase(AlertThrottle(), timeProvider)
    val processIncomingNotification by lazy {
        ProcessIncomingNotificationUseCase(
            appRules = appRulesRepository,
            settings = settingsRepository,
            alarmSettings = alarmSettingsRepository,
            shouldTrigger = shouldTriggerAlarm,
            startAlarm = startAlarmPlayback,
            history = alertHistoryRepository,
            timeProvider = timeProvider,
        )
    }
}
