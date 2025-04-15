package ru.igels.camerastream02.data.appsettings

import ru.igels.camerastream02.data.appsettings.models.SettingsModel
import ru.igels.camerastream02.domain.models.AppStateModel
import ru.igels.camerastream02.domain.models.CredentialsModel
import ru.igels.camerastreamer.shared.models.*

interface ISettingsData {
    val settings: SettingsModel

    fun saveSettings(set: SettingsModel, force: Boolean = false): Boolean
    fun getAppState(): AppStateModel
    fun setCameraList(cams: List<CameraInfoModel>)
    fun getCameraList(): List<CameraInfoModel>
    fun getCameraListShort(): List<Pair<CAMFACING, Int>>
    fun getResolutionList(cameraID: Int): List<String>
    fun getFpsList(): List<Int>
    fun getQualityList(): Array<QualityList>
    fun getFocisList(cameraID: Int): List<CAMFOCUSES>
    fun getFocusFromString(focus: String): CAMFOCUSES
    fun setIsCrash(value: Boolean)
    fun setState(state: AppStateModel)
    fun getRotation(): DeviceOrientation
    fun getBitrate(): Int
    fun getWidthHeight(): Pair<Int, Int>
    fun getFPS(): Int
    fun getCredentials(): CredentialsModel
}