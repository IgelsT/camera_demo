package ru.igels.camerastream02.domain.models

data class AppPermissions(
    var cameraPermission: Boolean = false,
    var microphonePermission: Boolean = false,
    var storagePermission: Boolean = false,
    var backgroundPermission: Boolean = false,
    var locationPermission: Boolean = false,
)