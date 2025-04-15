package ru.igels.camerastream02.domain

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaCodecInfo
import kotlinx.coroutines.*
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.encoders.VideoEncoder
import ru.igels.camerastream02.data.*
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.logger.ExternalLogger
import ru.igels.camerastream02.domain.logger.LoggerController
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog

import ru.igels.camerastream02.domain.models.*
import ru.igels.camerastream02.encoders.AudioEncoder
import ru.igels.camerastream02.mappers.ApiMappers
import ru.igels.camerastreamer.apicontroller.ApiController

import ru.igels.camerastream02.network.notneed.RtmpSender3
import ru.igels.camerastreamer.apicontroller.shared.API_ERROR_CODES
import ru.igels.camerastreamer.apicontroller.shared.API_MESSAGES
import ru.igels.camerastreamer.apicontroller.shared.ApiStatus
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelIN
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelOUT
import ru.igels.camerastreamer.camerav1.Camera1Core
import ru.igels.camerastreamer.camerav1.shared.CameraParamsModel
import ru.igels.camerastreamer.camerav1.shared.CameraState
import ru.igels.camerastreamer.camerav1.shared.ErrorCodes
import ru.igels.camerastreamer.camerav1.shared.ICameraCallBack
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class CameraController {

    private val logTag: String = "CameraController"
    private var cameraAPI: Camera1Core? = null
    private val frameProcessor = FrameProcessor()

    private var appMessageFlowHandler: Job? = null
    private var apiMessageFlowHandler: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val rawFrameFlowThread = newSingleThreadContext("rawFrameFlowThread")
    private var rawFrameFlowHandler: Job? = null

    private var codec = VideoEncoder()
    private var audioCodec = AudioEncoder()
    private var sender = RtmpSender3()

    private var lastWakeUpScreenTime = 0L
    private val wakeUpScreenTime = 10
    private val wakeUpScreenInterval = 60 * 1000L

    companion object {
        private var instance: CameraController? = null
        private val settingsData = SettingsData.getInstance()

        fun init(context: Context): CameraController {
            if (instance == null) instance = CameraController()
            return instance!!
        }

        private fun checkIsInit() {
            if (instance == null) throw Exception("init() CameraController first!")
        }

        fun setPreview(value: Boolean) {
            checkIsInit()
            val state = settingsData.getAppState()
            state.isPreview = value
            AppMessageBus.publish(AppMessage(APPMSG_TYPE.STATE_REQUESTED, state))
        }

        fun setStream(value: Boolean) {
            checkIsInit()
            val state = settingsData.getAppState()
            state.isStream = value
            AppMessageBus.publish(AppMessage(APPMSG_TYPE.STATE_REQUESTED, state))
        }
    }

    private val camCallBack = object : ICameraCallBack {
        override fun cameraState(state: CameraState) {
            when (state) {
                is CameraState.Busy -> {
                    iLog(logTag, "Camera state Busy ${state.message}")
                }

                is CameraState.Error -> {
                    iLog(logTag, "Camera state Error ${state.errorCode}")
                }

                is CameraState.Open -> {
                    iLog(logTag, "Camera state Opened ${state.message}")
                }

                is CameraState.Idle -> {
                    iLog(logTag, "Camera state Idle")
                }
            }
        }

        override fun rawDataCallBack(data: ByteArray) {
            FrameQueues.rawFrameFlow.tryEmit(data)
        }
    }

    init {
        iLog(logTag, "init")
        appMessageFlowHandler = CoroutineScope(Dispatchers.IO).launch {
            AppMessageBus.getAppMessageBusFlow().collect {
                iLog(logTag, "App bus message ${it.type} ${it.payload}")
                when (it.type) {
                    APPMSG_TYPE.STATE_REQUESTED -> {
                        settingsChanged(it.payload as AppStateModel)
                    }

                    APPMSG_TYPE.STATE_UPDATED -> ApiController.sendDeviceState(makeState())

                    APPMSG_TYPE.SETTINGS_UPDATED -> {
                        settingsChanged(settingsData.getAppState())
                        ApiController.sendDeviceInfo(
                            AndroidInfoData.buildDeviceInfo()
                        )
                        ApiController.sendDeviceState(makeState())
                    }

                    APPMSG_TYPE.PERMISSION_UPDATED -> {
                        if ((it.payload as AppPermissions).cameraPermission) {
                            cameraAPI = Camera1Core(camCallBack, ExternalLogger)
                            if (settingsData.getCameraList().isEmpty())
                                settingsData.setCameraList(cameraAPI!!.getCameraList())
                        }
                    }

                    APPMSG_TYPE.POWERSTATE_UPDATED -> {
                        ApiController.sendDeviceState(makeState())
                    }

                    APPMSG_TYPE.LOCATION_UPDATED -> {
                        ApiController.sendDeviceState(makeState())
                    }

                    APPMSG_TYPE.LOG_ROTATED -> {
                        ApiController.sendLogsList(LoggerController.getLogList())
                    }

                    else -> {}
                }
            }
        }

        apiMessageFlowHandler = CoroutineScope(Dispatchers.IO).launch {
            ApiController.getStatusFlow().collect {
                iLog(logTag, "API bus message ${it}")
                when (it) {
                    is ApiStatus.ApiError -> {
                        if (it.code == API_ERROR_CODES.LOGIN_ERROR) screenHack()
                    }

                    is ApiStatus.ApiLoginResult -> {
                        val camList = settingsData.getCameraList()
                        ApiController.sendCameraList(camList)
                        ApiController.sendLogsList(LoggerController.getLogList())
                        val set = settingsData.settings
                        set.deviceToken = it.deviceToken
                        set.rtmpAddress = it.rtmpAddress
                        settingsData.saveSettings(set)
                    }

                    is ApiStatus.ApiMessage<*> -> {
                        analizeApiMessage(it)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun analizeApiMessage(command: ApiStatus.ApiMessage<*>) {
        iLog(logTag, "API command $command")
        when (command.action) {
            API_MESSAGES.SETTINGS -> {
                if (command.data is DeviceStateModelIN) {
                    changeSettings(command.data as DeviceStateModelIN)
                    ApiController.sendCommandComplete(command.messageId)
                }
            }

            API_MESSAGES.LOGS -> {
                ApiController.sendLogsList(LoggerController.getLogList())
                ApiController.sendCommandComplete(command.messageId)
            }

            API_MESSAGES.LOG_FILE -> {
                if (command.data is String) {
                    val file = LoggerController.getFile(command.data as String)
                    if (file != null) ApiController.sendLogFile(file)
                    ApiController.sendCommandComplete(command.messageId)
                }
            }

            else -> {}
        }
    }

    private fun changeSettings(data: DeviceStateModelIN) {
        val set = ApiMappers.settingsFromApi(data)
        val state = settingsData.getAppState()
        state.isStream = data.device_status == 1
        settingsData.saveSettings(set)
        AppMessageBus.publish(AppMessage(APPMSG_TYPE.STATE_REQUESTED, state))
    }

    private fun makeState(): DeviceStateModelOUT {
        val settings = settingsData.settings
        val loc = DeviceLocationData.getLocation()
        var location = ""
        if (loc != null)
            location = loc.latitude.toString() + "," + loc.longitude
        val state = settingsData.getAppState()

        return DeviceStateModelOUT(
            device_uid = AndroidInfoData.getAndroidID(),
            device_name = settings.deviceName,
            device_description = settings.deviceDescription,
            device_camera_id = settings.cameraID,
            device_focus = settings.focus,
            device_resolution = "${settings.width}x${settings.height}",
            device_orientation = settings.rotation,
            device_fps = settings.fps,
            device_quality = settings.quality,
            device_status = if (state.isStream) 1 else 0,
            device_power = DevicePowerStateData.getPowerState(),
            device_location = location
        )
    }

    private fun screenHack() {
        val connectionState = DeviceNetworkStateData.getNetworkState()
        if (!connectionState && System.currentTimeMillis() - lastWakeUpScreenTime > wakeUpScreenInterval) {
            lastWakeUpScreenTime = System.currentTimeMillis()
            PowerController.wakeUpScreen(wakeUpScreenTime)
        }
    }


    private fun settingsChanged(state: AppStateModel) {
        if ((state.isStream || state.isPreview) && !PermissionData.cameraPermission) {
            eLog(logTag, "No camera permission")
            state.isStream = false
            state.isPreview = false
            settingsData.setState(state)
            return
        }
        if (settingsData.settings.deviceToken == "") {
            stopStream()
            stopCamera()
            state.isStream = false
            state.isPreview = false
            settingsData.setState(state)
            return
        }
        if (state.isStream || state.isPreview) state.isPreview = startCamera()
        else stopCamera()

        if (state.isStream) state.isStream = startStream()
        else stopStream()
        settingsData.setState(state)
    }

    private fun startCamera(): Boolean {
        if (cameraAPI == null) {
            eLog(logTag, "No camera API")
            return false
        }
        val settings = settingsData.settings
        val params = CameraParamsModel(
            settings.cameraID,
            settings.width,
            settings.height,
            settingsData.getRotation(),
            settings.focus,
        )
        var res = cameraAPI!!.start(params)
        if (res is CameraState.Error && res.errorCode == ErrorCodes.ERROR_OPEN_CAMERA) {
            PowerController.wakeUpScreen(3)
            res = cameraAPI!!.start(params)
        }
        if (res is CameraState.Open) {
            startFrameFlowListen()
            return true
        } else {
            if (res is CameraState.Error) {
                eLog(logTag, "Camera open error ${res.errorCode}, restart app")
            }
        }
        return false
    }

    private fun stopCamera() {
        stopFrameFlowListen()
        cameraAPI?.stop()
    }

    private fun startStream(): Boolean {
        val size = settingsData.getWidthHeight()
        val fps = settingsData.getFPS()
        val bitRate = settingsData.getBitrate()
        val credentials = settingsData.getCredentials()
        if (credentials.deviceToken == "") {
            eLog(logTag, "can not start stream device token empty!")
            return false
        }
        codec.start(size.first, size.second, fps, bitRate, codecInCallback, codecOutCallback)
//        audioCodec.start(codecOutCallbackAudio)
//        openFile()

        val url =
            "rtmp://" + credentials.rtmpAddress + "/live/" + AndroidInfoData.getAndroidID() + "?action=publish&authToken=" + credentials.deviceToken
        sender.start(url, size.first, size.second, fps)
        return true
    }

    private fun stopStream() {
        codec.stop()
        audioCodec.stop()
        sender.stop()
    }

    @SuppressLint("SimpleDateFormat")
    private fun startFrameFlowListen() {
        val set = settingsData.settings
        val rotation = settingsData.getRotation()
        if (rawFrameFlowHandler != null) stopFrameFlowListen()
        rawFrameFlowHandler = CoroutineScope(rawFrameFlowThread).launch {
            FrameQueues.rawFrameFlow.collect { data ->
                val sdf = SimpleDateFormat("dd.MM.yyy HH:mm:ss")
                val text = sdf.format(Date()).toString()
                val bmp = frameProcessor.processFrameNV21(
                    data,
                    set.width,
                    set.height,
                    rotation,
                    text
                )
                FrameQueues.framesFlow.tryEmit(bmp!!)
            }
        }
    }

    private fun stopFrameFlowListen() {
        rawFrameFlowHandler?.cancel()
        rawFrameFlowHandler = null
    }

    private val codecInCallback = fun(format: Int): ByteBuffer? {
        val data = FrameQueues.framesFlow.replayCache.lastOrNull()
        return if (data != null) {
            when (format) {
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> {
                    frameProcessor.processFrameRGBA2I420Planar(data)
                }
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> {
                    frameProcessor.processFrameRGBA2I420SemiPlanar(data)
                }
                else -> {
                    frameProcessor.processFrameRGBA2NV12(data)
                }
            }
        } else null
    }

    private val codecOutCallback = fun(frame: MediaCodecFrame) {
        FrameQueues.mediaCodecFrames.offer(frame)
    }

    private val codecOutCallbackAudio = fun(frame: MediaCodecFrameAudio) {
        FrameQueues.mediaCodecFramesAudio.offer(frame)
//        toFile(frame.data)
    }

    protected fun finalize() {
        appMessageFlowHandler?.cancel()
        apiMessageFlowHandler?.cancel()
        stopFrameFlowListen()
        iLog(logTag, "Finalize cameraController class")
    }
}