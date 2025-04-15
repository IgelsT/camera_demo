package ru.igels.camerastream02.ui.viewmodels

import androidx.lifecycle.ViewModel
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastreamer.shared.models.CAMFACING
import ru.igels.camerastreamer.shared.models.CAMFOCUSES
import ru.igels.camerastreamer.shared.models.QualityList


class SettingsViewModel : ViewModel() {


    private val logTag = "SettingsViewModel"

    private val settingsData = SettingsData.getInstance()
    private var settings = SettingsData.getInstance().settings

    private var cameraList: List<Pair<CAMFACING, Int>> = listOf()
    private var resolutionList: List<String> = listOf()
    private var fpsList: List<Int> = listOf()
    private var qualityList: Array<QualityList> = arrayOf()
    private var focusList: List<CAMFOCUSES> = listOf()

    fun saveSettings() {
        settingsData.saveSettings(settings)
    }

    fun getCameraList(): Pair<List<String>, Int> {
        cameraList = settingsData.getCameraListShort()
        return Pair(cameraList.map { el -> if (el.first == CAMFACING.BACK) "BACK" else "FRONT" }
            .toList(), settings.cameraID)
    }

    fun setCameraSelected(pos: Int) {
        settings.cameraID = cameraList[pos].second
    }

    fun getResolutionList(): Pair<List<String>, Int> {
        resolutionList = settingsData.getResolutionList(settings.cameraID)
        var selected = resolutionList.indexOf("${settings.width}x${settings.height}")
        if (selected == -1) selected = 0
        return Pair(resolutionList, selected)
    }

    fun setResolutionSelected(pos: Int) {
        val res = resolutionList[pos].split("x")
        settings.width = res[0].toInt()
        settings.height = res[1].toInt()
    }

    fun getFpsList(): Pair<List<String>, Int> {
        fpsList = settingsData.getFpsList()
        return Pair(fpsList.map { el -> el.toString() }.toList(), fpsList.indexOf(settings.fps))

    }

    fun setFpsSelected(pos: Int) {
        settings.fps = fpsList[pos]
    }

    fun getQualityList(): Pair<List<String>, Int> {
        qualityList = settingsData.getQualityList()
        return Pair(qualityList.map { el -> el.name }.toList(), settings.quality)
    }

    fun setQualitySelected(pos: Int) {
        settings.quality = pos
    }

    fun getFocusList(): Pair<List<String>, Int> {
        focusList = settingsData.getFocisList(settings.cameraID)
        return Pair(
            focusList.map { el -> el.name }.toList(),
            CAMFOCUSES.values().indexOf(settings.focus)
        )
    }

    fun setFocusSelected(pos: Int) {
        settings.focus = CAMFOCUSES.values()[pos]
    }

    fun getFlipVertical(): Boolean {
        return settings.flipVertical
    }

    fun setFlipVertical(value: Boolean) {
        settings.flipVertical = value
    }

    fun getAccSave(): Boolean {
        return settings.accSave
    }

    fun setAccSave(value: Boolean) {
        settings.accSave = value
    }

    fun getAutoStart(): Boolean {
        return settings.autoStart
    }

    fun setAutoStart(value: Boolean) {
        settings.autoStart = value
    }

    fun getServerAddress(): String {
        return settings.baseUrl
    }

    fun setServerAddress(value: String) {
        settings.baseUrl = value.trim()
    }

    fun getUserName(): String {
        return settings.userName.trim()
    }

    fun setUserName(value: String) {
        settings.userName = value.trim()
    }

    fun getUserPassword(): String {
        return settings.userPassword
    }

    fun setUserPassword(value: String) {
        settings.userPassword = value.trim()
    }

    fun getDeviceName(): String {
        return settings.deviceName
    }

    fun setDeviceName(value: String) {
        settings.deviceName = value.trim()
    }

    fun getDeviceDescr(): String {
        return settings.deviceDescription
    }

    fun setDeviceDescr(value: String) {
        settings.deviceDescription = value.trim()
    }
}