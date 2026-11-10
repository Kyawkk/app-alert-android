package com.kyawzinlinn.app.alert.feature.settings.domain

import kotlinx.coroutines.flow.Flow

class ObserveAppSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.observe()
}

class SetMonitoringEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setMonitoringEnabled(enabled)
}

class UpdateQuietHoursUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(transform: (QuietHours) -> QuietHours) {
        repository.updateQuietHours { current ->
            val updated = transform(current)
            updated.copy(
                startMinuteOfDay = updated.startMinuteOfDay.coerceIn(0, 24 * 60 - 1),
                endMinuteOfDay = updated.endMinuteOfDay.coerceIn(0, 24 * 60 - 1),
                days = updated.days.filter { it in 1..7 }.toSet(),
            )
        }
    }
}

class CompleteOnboardingUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke() = repository.setOnboardingCompleted(true)
}
