package com.syncboard.core.platform

actual object PlatformInfo {

    actual fun deviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = "JVM",
            model =
                System.getProperty("os.arch")
                    ?: "unknown",
            osVersion =
                System.getProperty("os.version")
                    ?: "unknown"
        )
    }
}
