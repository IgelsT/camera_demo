package ru.igels.camerastream02.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.data.FrameQueues
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.CameraController
import ru.igels.camerastream02.domain.logger.dLog
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppStateModel
import ru.igels.camerastreamer.apicontroller.ApiController
import ru.igels.camerastreamer.apicontroller.shared.ApiStatus

data class MainModelState(
    var isBusy: Boolean = false,
    var hasPermissions: Boolean = false,
    var hasToken: Boolean = false,
    var baseUrl: String = "",
    var userName: String = "",
    var userPassword: String = "",
    var isStream: Boolean = false,
    var isPreview: Boolean = false,
    var errorMessage: String = ""
)

enum class ActiveFragment {
    PERMISSION, LOGIN, MAIN, NONE
}

class MainModel : ViewModel() {
    val logTag = "MainViewModel"

    private val settingsData = SettingsData.getInstance()
    var settings = settingsData.settings
    val state = MainModelState(
        isBusy = false,
        hasPermissions = PermissionData.permissionsForStart,
        isPreview = settingsData.getAppState().isPreview,
        isStream = settingsData.getAppState().isStream,
        baseUrl = settingsData.getCredentials().baseUrl,
        userName = settingsData.getCredentials().userName,
        userPassword = settingsData.getCredentials().userPassword,
        hasToken = settingsData.getCredentials().deviceToken != "",
    )

    private val _mainActivityState = MutableStateFlow(state.copy())
    val mainActivityState = _mainActivityState.asStateFlow()

    private val _mainActivityViewState = MutableStateFlow(ActiveFragment.NONE)
    val mainActivityVewState = _mainActivityViewState.asStateFlow()

    init {
        dLog(logTag, "MainViewModel created")
        getMainViewState()
        viewModelScope.launch {
            AppMessageBus.getAppMessageBusFlow().filter {
                it.type == APPMSG_TYPE.STATE_UPDATED || it.type == APPMSG_TYPE.PERMISSION_UPDATED
                        || it.type == APPMSG_TYPE.SETTINGS_UPDATED
            }.collectLatest {
                dLog(logTag, "Get ${it.type} ${it.payload}")
                if (it.type == APPMSG_TYPE.PERMISSION_UPDATED || it.type == APPMSG_TYPE.SETTINGS_UPDATED) {
                    getMainViewState()
                } else if (it.type == APPMSG_TYPE.STATE_UPDATED) {
                    state.isStream = (it.payload as AppStateModel).isStream
                    state.isPreview = it.payload.isPreview
                }
                _mainActivityState.value = state.copy()
            }
        }
    }

    private fun getMainViewState() {
        if (!PermissionData.permissionsForStart)
            _mainActivityViewState.value = ActiveFragment.PERMISSION
        else if (settingsData.settings.deviceToken == "") {
            _mainActivityViewState.value = ActiveFragment.LOGIN
        } else _mainActivityViewState.value = ActiveFragment.MAIN
    }

    fun login() {
        _mainActivityState.value = state.copy(isBusy = true, errorMessage = "")
        settings = settingsData.settings
        viewModelScope.launch {
            val response = ApiController.manualLogin(state.baseUrl, state.userName, state.userPassword)
            if (response is ApiStatus.ApiError) state.errorMessage = response.message
            settings.baseUrl = state.baseUrl
            settings.userName = state.userName
            settings.userPassword = state.userPassword
            if (response is ApiStatus.ApiLoginResult) {
//                settings.accountToken = response.userToken
                settings.deviceToken = response.deviceToken
            }
            settingsData.saveSettings(settings)
            state.isBusy = false
            _mainActivityState.value = state.copy()
        }
    }

    fun logout() {
        settings = settingsData.settings
        settings.deviceToken = ""
        settings.accountToken = ""
        CameraController.setStream(false)
        settingsData.saveSettings(settings)
        ApiController.logout()
    }

    fun setCredentials(baseUrl: String, userName: String, userPassword: String) {
        state.baseUrl = baseUrl
        state.userName = userName
        state.userPassword = userPassword
        _mainActivityState.value = state.copy()
    }

    fun startPreview() {
        dLog(logTag, "StartPreview")
        state.isPreview = true
        CameraController.setPreview(true)
    }

    fun stopPreview() {
        state.isPreview = false
        viewModelScope.launch {
            delay(500)
            if (!state.isPreview) {
                dLog(logTag, "StopPreview")
                CameraController.setPreview(false)
            }
        }

    }

    fun getFrameQueue(): MutableSharedFlow<Bitmap> {
        return FrameQueues.framesFlow
    }

//    private fun getRotation(): Int {
//        val rotation =
//            MainApp.getInstance().applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        var degrees = 0
//        when (rotation.defaultDisplay.rotation) {
//            Surface.ROTATION_0 -> degrees = 90
//            Surface.ROTATION_90 -> degrees = 0
//            Surface.ROTATION_180 -> degrees = 270
//            Surface.ROTATION_270 -> degrees = 180
//        }
//        return degrees
//    }

    fun startStopStream() {
        CameraController.setStream(!state.isStream)
    }

    override fun onCleared() {
        super.onCleared()
        dLog(logTag, "MainViewModel finish")
    }
}