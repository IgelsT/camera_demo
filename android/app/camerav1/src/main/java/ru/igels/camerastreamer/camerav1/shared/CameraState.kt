package ru.igels.camerastreamer.camerav1.shared

enum class ErrorCodes {
    ERROR_OPEN_CAMERA,
    CAMERA_PROCESS_TIMEOUT,
    ERROR_CAMERA,
    ERROR_START_PREVIEW,
    ERROR_CLOSE_CAMERA
}

sealed class CameraState {
    object Idle : CameraState()
    class Busy(var message: String = "") : CameraState()
    class Open(var message: String = "") : CameraState()
    class Error(var errorCode: ErrorCodes, var errorMessage: String? = "") : CameraState()
}
