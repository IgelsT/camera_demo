package ru.igels.camerastream02.data.appsettings

import android.annotation.SuppressLint
import android.content.Context
import android.view.OrientationEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.igels.camerastream02.appmessagebus.IAppMessageBus
import ru.igels.camerastream02.data.appsettings.models.*
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.domain.models.*
import ru.igels.camerastreamer.shared.models.CAMFACING
import ru.igels.camerastreamer.shared.models.CAMFOCUSES
import ru.igels.camerastreamer.shared.models.CameraInfoModel
import ru.igels.camerastreamer.shared.models.DeviceOrientation
import ru.igels.camerastreamer.shared.models.QualityList
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class SettingsData private constructor(
    private val appContext: Context,
    settingsName: String,
    private val messageBus: IAppMessageBus
) :
    ISettingsData {

    private var cameraList: List<CameraInfoModel> = emptyList()
    private val logTag = "SettingsController"
    private val mPref = appContext.getSharedPreferences(settingsName, Context.MODE_PRIVATE)
    private var mOrientationListener: OrientationEventListener
    private val SETTINGS_STR = "settings"
    private val CAMLIST_STR = "camlist"
    private var _settings = SettingsModel()
    private var appState = AppStateModel()
    private var credentials = CredentialsModel()
    private val mLock = ReentrantLock(true)

    override val settings: SettingsModel
        @Synchronized
        get() {
            val s = _settings.copy()
            return s
        }

    companion object {
        private var instance: SettingsData? = null

        fun init(
            appContext: Context,
            settingsName: String,
            messageBus: IAppMessageBus
        ): SettingsData {
            if (instance == null) instance = SettingsData(appContext, settingsName, messageBus)
            return instance!!
        }

        fun getInstance(): ISettingsData {
            if (instance == null) throw Exception("init() settings first!")
            return instance!!
        }
    }

    init {
        iLog(logTag, "init")
        loadSettings()
        updateCredentials()
        mOrientationListener = object : OrientationEventListener(appContext) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == 0 || orientation == 90 || orientation == 180 || orientation == 270) {
                    val set = settings.copy()
                    when (orientation) {
                        0 -> set.rotation = DeviceOrientation.TOP//90
                        90 -> set.rotation = DeviceOrientation.LEFT//180
                        180 -> set.rotation = DeviceOrientation.BOTTOM//270
                        270 -> set.rotation = DeviceOrientation.RIGHT//0
                    }
//                    iLog(logTag, "rotation $orientation to ${set.rotation}")
                    saveSettings(set)
                }
            }
        }

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable()
        }
    }

    private fun loadSettings() {
        var settings = SettingsModel()
        val json = mPref.getString(SETTINGS_STR, null) ?: return
        try {
            settings = Gson().fromJson(json, SettingsModel::class.java)
            checkSettings(settings)
        } catch (e: Exception) {
            eLog(logTag, "Error load settings ${e.message}")
        }

        this._settings = settings

        val jsonCamList = mPref.getString(CAMLIST_STR, null) ?: return
        try {
            val itemType = object : TypeToken<List<CameraInfoModel>>() {}.type
            val list = Gson().fromJson<List<CameraInfoModel>>(jsonCamList, itemType)
            if (list.isEmpty()) return
            cameraList = list
        } catch (e: Exception) {
            eLog(logTag, "Error load camera list from store ${e.message}")
        }
    }

    @Synchronized
    override fun saveSettings(set: SettingsModel, force: Boolean): Boolean {
//        mLock.tryLock(1, TimeUnit.SECONDS)
        if (set == settings && !force) return true
        if (saveToStorage(SETTINGS_STR, set)) {
            _settings = set.copy()
            updateCredentials()
            messageBus.publish(AppMessage(APPMSG_TYPE.SETTINGS_UPDATED, settings))
//            if (mLock.isLocked) mLock.unlock()
            return true
        }
//        if (mLock.isLocked) mLock.unlock()
        return false
    }

    override fun getAppState(): AppStateModel = appState.copy()

    private fun updateCredentials() {
        credentials.accountToken = settings.accountToken
        credentials.deviceToken = settings.deviceToken
        credentials.baseUrl = settings.baseUrl
        credentials.userName = settings.userName
        credentials.userPassword = settings.userPassword
        credentials.rtmpAddress = settings.rtmpAddress
    }

    @SuppressLint("ApplySharedPref")
    private fun <T> saveToStorage(name: String, payload: T): Boolean {
        return try {
            val editor = mPref?.edit()
            editor?.putString(name, Gson().toJson(payload))
            editor?.commit()
            true
        } catch (e: Exception) {
            eLog(logTag, "Error save to storage $name settings ${e.message}")
            false
        }
    }

    override fun setCameraList(cams: List<CameraInfoModel>) {
        if (cams.isEmpty()) return
        cameraList = cams
        saveToStorage(CAMLIST_STR, cameraList)
        messageBus.publish(AppMessage(APPMSG_TYPE.CAMLIST_UPDATED, cameraList))
        val set = settings.copy()
        val resolutionList = getResolutionList(settings.cameraID)
        val selectedRes = resolutionList.indexOf("${settings.width}x${settings.height}")
        if (selectedRes == -1) {
            val res = resolutionList[0].split("x")
            set.width = res[0].toInt()
            set.height = res[1].toInt()
        }
        val focuses = getFocisList(settings.cameraID)
        val selectedFoc = focuses.indexOf(settings.focus)
        if (selectedFoc == -1 && focuses.isNotEmpty()) {
            set.focus = focuses[0]
        }
        if (selectedRes == -1 || selectedFoc == -1)
            saveSettings(set)
    }

    override fun getCameraList(): List<CameraInfoModel> = cameraList

    override fun getCameraListShort(): List<Pair<CAMFACING, Int>> =
        cameraList.map { el ->
            Pair(el.facing, el.cameraID)
        }.toList()

    override fun getResolutionList(cameraID: Int): List<String> =
        cameraList.first { el -> el.cameraID == cameraID }.res.map { el ->
            "${el.first}x${el.second}"
        }.toList()

    override fun getFpsList(): List<Int> = listOf(5, 10, 15, 20, 25)

    override fun getQualityList(): Array<QualityList> = QualityList.values()

    override fun getFocisList(cameraID: Int): List<CAMFOCUSES> =
        cameraList.first { el -> el.cameraID == cameraID }.focuses

    override fun getFocusFromString(focus: String): CAMFOCUSES = CAMFOCUSES.valueOf(focus)

    override fun setIsCrash(value: Boolean) {
        _settings.isCrashed = value
        saveToStorage(SETTINGS_STR, _settings)
    }

    override fun setState(state: AppStateModel) {
        if (appState.isPreview != state.isPreview || appState.isStream != state.isStream) {
            appState.isPreview = state.isPreview
            appState.isStream = state.isStream
            messageBus.publish(AppMessage(APPMSG_TYPE.STATE_UPDATED, appState.copy()))
        }
    }

    override fun getRotation(): DeviceOrientation {
        val rotation = instance!!.settings.rotation
        val flipVertical = instance!!.settings.flipVertical
        if (flipVertical) {
            if (rotation == DeviceOrientation.TOP) return DeviceOrientation.BOTTOM
            if (rotation == DeviceOrientation.BOTTOM) return DeviceOrientation.TOP
            if (rotation == DeviceOrientation.LEFT) return DeviceOrientation.RIGHT
            if (rotation == DeviceOrientation.RIGHT) return DeviceOrientation.LEFT
        }
        return rotation
    }

    override fun getBitrate(): Int {
        // LOW, MEDIUM, NORMAL, HIGH, MAX
        val baseBitrate = _settings.width * _settings.height * 3
        val divider: Int = when (_settings.quality) {
            0 -> 5
            1 -> 4
            2 -> 3
            3 -> 2
            4 -> 1
            else -> {
                5
            }
        }
        return (baseBitrate / divider)
    }

    override fun getWidthHeight(): Pair<Int, Int> {
        val width = instance!!.settings.width
        val height = instance!!.settings.height
        val rotation = instance!!.settings.rotation
        return if (rotation == DeviceOrientation.TOP || rotation == DeviceOrientation.BOTTOM) {
            Pair(height, width)
        } else Pair(width, height)
    }

    override fun getFPS(): Int = _settings.fps
    override fun getCredentials(): CredentialsModel = credentials
}