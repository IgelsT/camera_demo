package ru.igels.camerastream02.data.appsettings.models

import ru.igels.camerastreamer.shared.models.CAMFOCUSES
import ru.igels.camerastreamer.shared.models.DeviceOrientation


data class SettingsModel(
    var cameraID: Int = 0,
    var width: Int = 320,
    var height: Int = 240,
    var fps: Int = 15,
    var rotation: DeviceOrientation = DeviceOrientation.TOP,
    var focus: CAMFOCUSES = CAMFOCUSES.AUTO,
    var quality: Int = 2,
    var flipVertical: Boolean = false,
    var accSave: Boolean = false,
    var deviceName: String = "Camera 1",
    var deviceDescription: String = "Indoor camera 1",
    var userName: String = "",
    var userPassword: String = "",
    var accountToken: String = "",
    var deviceToken: String = "",
    var baseUrl: String = "camera.imile.ru",
    var autoStart: Boolean = false,
    var isCrashed: Boolean = false,
    var restartOnCrash: Boolean = false,
    var rtmpAddress: String = "camera.imile.ru"
) {
//    fun deepCopy(): SettingsModel {
//        return Gson().fromJson(Gson().toJson(this), this.javaClass)
//    }
}

fun checkSettings(newSettings: SettingsModel) {
    val settings = SettingsModel()
    if (newSettings.cameraID == null) newSettings.cameraID = settings.cameraID
    if (newSettings.width == null) newSettings.width = settings.width
    if (newSettings.height == null) newSettings.height = settings.height
    if (newSettings.fps == null) newSettings.fps = settings.fps
    if (newSettings.rotation == null) newSettings.rotation = settings.rotation
    if (newSettings.focus == null) newSettings.focus = settings.focus
    if (newSettings.quality == null) newSettings.quality = settings.quality
    if (newSettings.flipVertical == null) newSettings.flipVertical = settings.flipVertical
    if (newSettings.accSave == null) newSettings.accSave = settings.accSave
    if (newSettings.deviceName == null) newSettings.deviceName = settings.deviceName
    if (newSettings.deviceDescription == null) newSettings.deviceDescription =
        settings.deviceDescription
    if (newSettings.userName == null) newSettings.userName = settings.userName
    if (newSettings.userPassword == null) newSettings.userPassword = settings.userPassword
    if (newSettings.accountToken == null) newSettings.accountToken = settings.accountToken
    if (newSettings.deviceToken == null) newSettings.deviceToken = settings.deviceToken
    if (newSettings.isCrashed == null) newSettings.isCrashed = settings.isCrashed
    if (newSettings.autoStart == null) newSettings.autoStart = settings.autoStart
    settings.restartOnCrash = true
    if (newSettings.baseUrl == null) newSettings.baseUrl = newSettings.baseUrl
    if (newSettings.rtmpAddress == null) newSettings.rtmpAddress = newSettings.rtmpAddress
}