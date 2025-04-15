package ru.igels.camerastream02.domain.models

data class AppStateModel(
    var isStream: Boolean = false,
    var isPreview: Boolean = false,
    var isLogin: Boolean = false
)
