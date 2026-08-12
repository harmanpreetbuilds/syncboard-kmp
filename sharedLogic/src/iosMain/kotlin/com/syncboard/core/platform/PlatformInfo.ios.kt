package com.syncboard.core.platform

import platform.UIKit.UIDevice

actual object PlatformInfo {

    actual fun deviceInfo(): DeviceInfo {
        val device = UIDevice.currentDevice

        return DeviceInfo(
            platform = "iOS",
            model = device.model,
            osVersion = device.systemVersion
        )
    }
}
