package com.syncboard.core.platform

data class DeviceInfo(
    val platform: String,
    val model: String,
    val osVersion: String
)

expect object PlatformInfo {
    fun deviceInfo(): DeviceInfo
}
