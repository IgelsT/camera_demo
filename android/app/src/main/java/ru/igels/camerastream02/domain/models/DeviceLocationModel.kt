package ru.igels.camerastream02.domain.models

import android.location.Location

data class DeviceLocationModel(
    var permissions: Boolean = false,
    var gpsProviderEnabled: Boolean = false,
    var networkProviderEnabled: Boolean = false,
    var location: Location? = null
) {
}