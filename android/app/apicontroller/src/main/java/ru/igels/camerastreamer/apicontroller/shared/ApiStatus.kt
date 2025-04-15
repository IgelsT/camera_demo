package ru.igels.camerastreamer.apicontroller.shared

sealed class ApiStatus() {
    class ApiError(
        val action: String,
        val code: API_ERROR_CODES,
        val message: String
    ) : ApiStatus()

    class ApiLoginResult(
        val userToken: String = "",
        val deviceToken: String = "",
        val rtmpAddress: String = ""
    ) : ApiStatus()

    class ApiMessage<T>(
        val messageId: Int,
        val action: API_MESSAGES,
        val data: T?
    ): ApiStatus()
}