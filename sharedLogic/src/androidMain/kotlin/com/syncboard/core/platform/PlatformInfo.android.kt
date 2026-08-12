package com.syncboard.core.platform

import android.os.Build

actual object PlatformInfo {

    actual fun deviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = "Android",
            model = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
            osVersion = Build.VERSION.RELEASE
        )
    }
}
