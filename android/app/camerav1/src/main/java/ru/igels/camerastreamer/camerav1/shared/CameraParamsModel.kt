package ru.igels.camerastreamer.camerav1.shared

class CameraParamsModel(
    val cameraID: Int,
    val width: Int,
    val height: Int,
    val rotation: ru.igels.camerastreamer.shared.models.DeviceOrientation,
    val focus: ru.igels.camerastreamer.shared.models.CAMFOCUSES,
)