package ru.igels.camerastreamer.apicontroller.response.models

internal data class LoginModel(
    val device_token: String,
    val user_id: Int,
    val user_name: String,
    val user_email: String,
    val rtmp_address: String,
) {
}