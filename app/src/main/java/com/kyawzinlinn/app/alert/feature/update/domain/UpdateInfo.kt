package com.kyawzinlinn.app.alert.feature.update.domain

data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val apkSizeBytes: Long,
    val sha256: String,
    val releaseUrl: String,
)

enum class UpdateFailure {
    NO_NETWORK,
    RATE_LIMITED,
    BAD_RESPONSE,
    STORAGE,
    CHECKSUM_MISMATCH,
    WRONG_PACKAGE,
    WRONG_SIGNATURE,
    INSTALL_NOT_ALLOWED,
    NO_INSTALLER,
}
