package com.kyawzinlinn.app.alert.feature.dashboard.domain

/** What is really happening, derived from permission, service binding and user configuration. */
enum class MonitoringStatus {
    ACTIVE,
    PAUSED_BY_USER,
    NOTIFICATION_ACCESS_MISSING,
    LISTENER_DISCONNECTED,
    NO_APPS_SELECTED,
    IN_QUIET_HOURS,
}
