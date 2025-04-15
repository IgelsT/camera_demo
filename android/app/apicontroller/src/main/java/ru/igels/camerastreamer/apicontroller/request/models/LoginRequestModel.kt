package ru.igels.camerastreamer.apicontroller.request.models

internal data class LoginRequestModel(
    val user_email: String,
    val user_password: String,
    val device_uid: String
)