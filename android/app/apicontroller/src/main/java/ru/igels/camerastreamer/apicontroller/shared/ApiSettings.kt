package ru.igels.camerastreamer.apicontroller.shared

data class ApiSettings(
    var url: String = "",
    var userName: String = "",
    var userPassword: String = "",
    var userToken: String = "",
    var deviceToken: String = "",
    var deviceUid: String = "",
)
