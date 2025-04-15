package ru.igels.camerastreamer.apicontroller.response.models

import ru.igels.camerastreamer.shared.models.CAMFOCUSES
import ru.igels.camerastreamer.shared.models.DeviceOrientation
import ru.igels.camerastreamer.apicontroller.common.Utils
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelIN

internal data class MessageSettingsModel(
    val device_name: String = "",
    val device_description: String = "",
    val device_camera_id: Int = 0,
    val device_focus: String = "",
    val device_resolution: String = "",
    val device_orientation: String = "",
    val device_fps: Int = 0,
    val device_quality: Int = 0,
    val device_status: Int =0 ,
    val rtmp_address: String = "",
)  {
    fun toOUT(): DeviceStateModelIN {
        return DeviceStateModelIN(
            device_name = device_name,
            device_description = device_description,
            device_camera_id = device_camera_id,
            device_focus = Utils.enumFromString<CAMFOCUSES>(device_focus),
            device_resolution = device_resolution,
            device_orientation = Utils.enumFromString<DeviceOrientation>(device_orientation),
            device_fps = device_fps,
            device_quality = device_quality,
            device_status = device_status,
            rtmp_address = rtmp_address
        )
    }
}