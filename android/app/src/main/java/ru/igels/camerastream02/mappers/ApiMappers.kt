package ru.igels.camerastream02.mappers

import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.data.appsettings.models.SettingsModel
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelIN
import ru.igels.camerastreamer.shared.models.DeviceOrientation

object ApiMappers {
    fun settingsFromApi(state: DeviceStateModelIN): SettingsModel {
        val settings = SettingsData.getInstance().settings

        val res = getResolutionFromString(state.device_resolution)

        settings.deviceName = state.device_name
        settings.deviceDescription = state.device_description
        settings.cameraID = state.device_camera_id
        settings.focus = state.device_focus
        settings.width = res.first
        settings.height = res.second
        settings.rotation = state.device_orientation
        settings.fps = state.device_fps
        settings.quality = state.device_quality
        if (state.rtmp_address != "") settings.rtmpAddress = settings.rtmpAddress

        return settings
    }

    private fun getResolutionFromString(str: String): Pair<Int, Int> {
        val res = str.split("x")
        var width = 320
        var height = 240
        if (res.size == 2) {
            try {
                width = res[0].toInt()
                height = res[1].toInt()
            } catch (nfe: NumberFormatException) {
                // not a valid int
            }
        }
        return Pair(width, height)
    }
//
//    fun facingToApi(facing: CAMFACING): HTTP_CAMFACING {
//        return HTTP_CAMFACING.values().find {
//            it.toString() == facing.toString()
//        } ?: HTTP_CAMFACING.UNKNOWN
//    }
//
//    fun camListToApi(camList: List<CameraInfoModel>): List<DeviceCameraModel> {
//        return camList.map { camera ->
//            DeviceCameraModel(
//                camera.cameraID,
//                facingToApi(camera.facing),
//                camera.res,
//                camFocusesToApi(camera.focuses)
//            )
//        }
//    }
//
//    fun camFocusToApi(camFocus: CAMFOCUSES): HTTP_CAMFOCUSES {
//        return ru.igels.camerastreamer.apicontroller.shared.CAMFOCUSES.values().find {
//            it.toString() == camFocus.toString()
//        } ?: ru.igels.camerastreamer.apicontroller.shared.CAMFOCUSES.AUTO
//    }
//
//    fun camFocusFromApi(camFocus: HTTP_CAMFOCUSES): CAMFOCUSES {
//        return CAMFOCUSES.values().find {
//            it.toString() == camFocus.toString()
//        } ?: CAMFOCUSES.AUTO
//    }
//
//    fun camFocusesToApi(camFocuses: List<CAMFOCUSES>): List<HTTP_CAMFOCUSES> =
//        camFocuses.map { focus -> camFocusToApi(focus) }
//
    fun orientationToApi(rotation: Int): DeviceOrientation {
        return when (rotation) {
            0 -> DeviceOrientation.RIGHT
            90 -> DeviceOrientation.TOP
            180 -> DeviceOrientation.LEFT
            270 -> DeviceOrientation.BOTTOM
            else -> DeviceOrientation.UNKNOWN
        }
    }
//
    fun orientationFromApi(rotation: DeviceOrientation): Int {
        return when (rotation) {
            DeviceOrientation.RIGHT -> 0
            DeviceOrientation.TOP -> 90
            DeviceOrientation.LEFT -> 180
            DeviceOrientation.BOTTOM -> 270
            DeviceOrientation.UNKNOWN -> 90
        }
    }
}