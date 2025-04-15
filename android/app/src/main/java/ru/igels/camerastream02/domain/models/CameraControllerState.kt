package ru.igels.camerastream02.domain.models

sealed class CameraControllerState {
    enum class ErrorCodes {
        ERROR_OPEN_CAMERA,
        ERROR_START_MEDIACODEC,
        ERROR_SERVER_URL_EMPTY
    }

    object Idle : CameraControllerState()
    object Busy : CameraControllerState()
    object Streaming : CameraControllerState()
    class Error(var errorCode: ErrorCodes, var errorMessage: String? = "") : CameraControllerState()
}
