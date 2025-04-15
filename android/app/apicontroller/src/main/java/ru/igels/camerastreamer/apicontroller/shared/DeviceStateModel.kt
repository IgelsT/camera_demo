package ru.igels.camerastreamer.apicontroller.shared

import ru.igels.camerastreamer.shared.models.CAMFOCUSES
import ru.igels.camerastreamer.shared.models.DeviceOrientation

data class DeviceStateModelIN( // From API
    val device_name: String,
    val device_description: String,
    val device_camera_id: Int,
    val device_focus: CAMFOCUSES,
    val device_resolution: String,
    val device_orientation: DeviceOrientation,
    val device_fps: Int,
    val device_quality: Int,
    val device_status: Int,
    val rtmp_address: String
)

data class DeviceStateModelOUT(  // To API
    val device_uid: String,
    val device_name: String,
    val device_description: String,
    val device_camera_id: Int,
    val device_focus: CAMFOCUSES,
    val device_resolution: String,
    val device_orientation: DeviceOrientation,
    val device_fps: Int,
    val device_quality: Int,
    val device_power: Int,
    val device_status: Int,
    val device_location: String,
)