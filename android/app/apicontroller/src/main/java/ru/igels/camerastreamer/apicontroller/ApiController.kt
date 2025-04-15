package ru.igels.camerastreamer.apicontroller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import ru.igels.camerastreamer.apicontroller.HTTPSender.HTTPSender
import ru.igels.camerastreamer.apicontroller.HTTPSender.IHTTPSender
import ru.igels.camerastreamer.apicontroller.request.ApiRequest
import ru.igels.camerastreamer.apicontroller.request.ApiRequestFactory
import ru.igels.camerastreamer.apicontroller.request.ApiRequestPing
import ru.igels.camerastreamer.apicontroller.response.ApiResponse
import ru.igels.camerastreamer.apicontroller.response.ApiResponseLogin
import ru.igels.camerastreamer.apicontroller.shared.API_ERROR_CODES
import ru.igels.camerastreamer.shared.logger.ILogger
import ru.igels.camerastreamer.apicontroller.common.Logger
import ru.igels.camerastreamer.apicontroller.response.ApiResponseBlank
import ru.igels.camerastreamer.apicontroller.response.ApiResponseFactory.analizeApiMessage
import ru.igels.camerastreamer.apicontroller.response.ApiResponsePing
import ru.igels.camerastreamer.apicontroller.shared.apiSettings
import ru.igels.camerastreamer.apicontroller.shared.ApiSettings
import ru.igels.camerastreamer.apicontroller.shared.ApiStatus
import ru.igels.camerastreamer.shared.models.DeviceInfoModel
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelOUT
import ru.igels.camerastreamer.shared.models.CameraInfoModel
import java.io.File

import java.util.concurrent.LinkedBlockingDeque

class ApiController {

    private val HTTPSender: IHTTPSender = HTTPSender()
    private val messagesQueue = LinkedBlockingDeque<ApiRequest>(10)
    private var isLogin = false;
    private val apiStatusFlow =
        MutableSharedFlow<ApiStatus>(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @OptIn(DelicateCoroutinesApi::class)
    private val apiFlowThread = newSingleThreadContext("apiFlowThread")
    private var queueJob: Job? = null
    private var pingJob: Job? = null
    private var delayJob: Job? = null
    private val queueAskInterval = 1000L
    private val pingInterval = 10 * 1000L
    private val delayTime = 30 * 1000L

    companion object {
        private const val logTag = "ApiController"
        private var instance: ApiController? = null

        fun init(settings: ApiSettings, logger: ILogger? = null): ApiController {
            apiSettings = settings
            Logger.externalLogger = logger
            if (instance == null) instance = ApiController()
            return instance!!
        }

        private fun checkIsInit(): ApiController? {
            if (instance == null) throw Exception("init() ApiController first!")
            return instance
        }

        fun getStatusFlow(): SharedFlow<ApiStatus> = checkIsInit()!!.apiStatusFlow.asSharedFlow()

        suspend fun manualLogin(apiUrl: String, userName: String, userPassword: String): ApiStatus {
            apiSettings.url = apiUrl
            apiSettings.userName = userName
            apiSettings.userPassword = userPassword
            return checkIsInit()!!.manualLoginRequest()
        }

        fun logout() {
            Logger.iLog(logTag, "logout")
            checkIsInit()?.isLogin = false
            checkIsInit()?.stopQueue()
            apiSettings.userToken = ""
            apiSettings.deviceToken = ""
        }

        fun sendDeviceInfo(info: DeviceInfoModel) =
            checkIsInit()?.sendDeviceInfo(info)

        fun sendDeviceState(state: DeviceStateModelOUT) = checkIsInit()?.sendDeviceState(state)
        fun sendCameraList(list: List<CameraInfoModel>) = checkIsInit()?.sendCameraList(list)
        fun sendLogsList(list: List<String>) = checkIsInit()?.sendLogsList(list)
        fun sendCommandComplete(id: Int) = checkIsInit()?.sendCommandComplete(id)
        fun sendLogFile(file: File) = checkIsInit()?.sendLogFile(file)
    }

    init {
        Logger.iLog(logTag, "init")
        if (apiSettings.deviceToken != "") autoLoginRequest()
    }

    private fun startQueue() {
        queueJob = startRepeatingJob(queueAskInterval) {
            askQueue()
        }
    }

    private fun startPing() {
        pingJob = startRepeatingJob(pingInterval) {
            ping()
        }
    }

    private fun stopQueue() {
        queueJob?.cancel()
        pingJob?.cancel()
        delayJob?.cancel()
        messagesQueue.clear()
    }

    private fun startRepeatingJob(timeInterval: Long, handler: () -> Unit): Job {
        return CoroutineScope(apiFlowThread).launch {
            while (isActive) {
                handler()
                delay(timeInterval)
            }
        }
    }

    private fun startDelayingJob(timeInterval: Long, handler: () -> Unit): Job {
        return CoroutineScope(apiFlowThread).launch {
            delay(timeInterval)
            handler()
        }
    }

    private suspend fun directRequest(apiRequest: ApiRequest): ApiResponse {
        return withContext(apiFlowThread) {
            return@withContext HTTPSender.sendRequest(apiRequest)
        }
    }

    //region Login_functions
    private suspend fun manualLoginRequest(): ApiStatus {
        val loginRequest = ApiRequestFactory.loginRequest()
        val response = directRequest(loginRequest)
        Logger.iLog(logTag, "Manual login request")

        val err = checkLoginFail(response)
        if (err != null) return err

        val lok = setLoginOK(response)
        apiStatusFlow.tryEmit(lok)
        startQueue()
        startPing()
        return lok
    }

    private fun autoLoginRequest() {
        val loginRequest = ApiRequestFactory.loginRequest()
        { request: ApiRequest, response: ApiResponse ->
            run {
                if (response !is ApiResponseLogin) return@loginRequest
                Logger.iLog(logTag, "Auto login response")
                val err = checkLoginFail(response)
                if (err != null) {
                    delayJob = startDelayingJob(delayTime) {
                        addToMessageQueue(request, force = true)
                    }
                    Logger.eLog(logTag, "Auto login error")
                    apiStatusFlow.tryEmit(err)
                    return@loginRequest
                }
                apiStatusFlow.tryEmit(setLoginOK(response))
                startPing()
            }
        }
        Logger.iLog(logTag, "Auto login request")
        startQueue()
        addToMessageQueue(loginRequest, force = true)
    }

    private fun checkLoginFail(response: ApiResponse): ApiStatus? {
        if (response.error != null && !response.success)
            return ApiStatus.ApiError(
                response.action,
                API_ERROR_CODES.LOGIN_ERROR,
                response.error!!.message
            )

        if (response is ApiResponseLogin &&
            (response.data?.device_token.isNullOrBlank() ||
                    response.data?.rtmp_address.isNullOrBlank())
        )
            return ApiStatus.ApiError(
                response.action,
                API_ERROR_CODES.LOGIN_ERROR,
                "Login error. Empty answer"
            )

        return null
    }

    private fun setLoginOK(response: ApiResponse): ApiStatus {
//        apiSettings.userToken = (response as ApiResponseLogin).data?.hash!!
        apiSettings.deviceToken = (response as ApiResponseLogin).data?.device_token!!
        isLogin = true
        return ApiStatus.ApiLoginResult(
            apiSettings.userToken,
            apiSettings.deviceToken,
            response.data?.rtmp_address ?: ""
        )
    }
//endregion

    fun sendDeviceInfo(info: DeviceInfoModel) {
        val deviceInfoRequest = ApiRequestFactory.deviceInfoRequest(info)
        addToMessageQueue(deviceInfoRequest)
        Logger.dLog(logTag, "send device info")
    }

    fun sendDeviceState(state: DeviceStateModelOUT) {
        val deviceStateRequest = ApiRequestFactory.deviceStateRequest(state)
        addToMessageQueue(deviceStateRequest)
        Logger.dLog(logTag, "send device state")
    }

    fun sendCameraList(list: List<CameraInfoModel>) {
        val deviceCameraListRequest = ApiRequestFactory.deviceCameraListRequest(list)
        addToMessageQueue(deviceCameraListRequest)
        Logger.dLog(logTag, "send device camera list")
    }

    fun sendLogsList(list: List<String>) {
        val logListRequest = ApiRequestFactory.logListRequest(list)
        addToMessageQueue(logListRequest)
        Logger.dLog(logTag, "send logs list")
    }

    private fun sendLogFile(file: File) {
        val logFileRequest = ApiRequestFactory.logFileRequest(file)
        addToMessageQueue(logFileRequest)
        Logger.dLog(logTag, "send log file $file")
    }

    private fun sendCommandComplete(id: Int) {
        val completeCommandRequest = ApiRequestFactory.completeCommandRequest(id)
        addToMessageQueue(completeCommandRequest)
        Logger.dLog(logTag, "send complete command $id")
    }

    private fun addToMessageQueue(
        packet: ApiRequest,
        first: Boolean = false,
        force: Boolean = false
    ) {
        if (!isLogin && !force) return
        try {
            for (pkg in messagesQueue) {
                if (pkg == packet) return
            }
            if (first) messagesQueue.addFirst(packet)
            else messagesQueue.addLast(packet)
        } catch (e: Exception) {
            Logger.eLog(logTag, "message queue is full")
        }
    }

    private fun askQueue() {
//        iLog(logTag, "Queue ask")
        if (messagesQueue.isNotEmpty()) {
            val request = messagesQueue.poll()
            if (request != null) {
                if (request.url.isBlank()) return
                Logger.dLog(logTag, "API Request: ${request.url} - ${request.action}")
                val response = HTTPSender.sendRequest(request)
                if (request.doneCallback != null) request.doneCallback!!(request, response)
                else analizePacket(request, response)
            }
        }
    }

    private fun analizePacket(request: ApiRequest, response: ApiResponse) {
        Logger.dLog(logTag, "Analize response for ${request.url} ${request.action}")
        if (response is ApiResponseBlank) return
        if (!response.success) {
            Logger.eLog(logTag, "response error ${response.error}")
            apiStatusFlow.tryEmit(
                ApiStatus.ApiError(
                    request.action,
                    response.error?.code ?: API_ERROR_CODES.UNKNOWN_ERROR,
                    response.error?.message ?: "Unknown error"
                )
            )

            if (response.error?.code == API_ERROR_CODES.BAD_TOKEN ||
                response.error?.code == API_ERROR_CODES.WRONG_PASSWORD ||
                response.error?.code == API_ERROR_CODES.USER_NOT_CONFIRM
            ) {
                stopQueue()
                autoLoginRequest()
                return
            }

            if (request !is ApiRequestPing) delayJob = startDelayingJob(delayTime) {
                addToMessageQueue(request)
            }
        }
    }

    private fun ping() {
        val pingRequest = ApiRequestFactory.pingRequest() { _: ApiRequest, response: ApiResponse ->
            run {
                if (response !is ApiResponsePing) return@pingRequest
                Logger.dLog(logTag, "Ping response analize ${response}")

                if (response.data?.messages == null) return@pingRequest

                val idList = mutableListOf<Int>()
                for (msg in response.data.messages) {
                    val command = analizeApiMessage(msg) ?: continue
                    Logger.dLog(logTag, command.toString())
                    apiStatusFlow.tryEmit(
                        ApiStatus.ApiMessage(
                            command.messageId, command.action, command.data
                        )
                    )
                    idList.add(command.messageId)
                }
                if (idList.isNotEmpty())
                    addToMessageQueue(ApiRequestFactory.appliedMessagesRequest(idList))
            }
        }
        addToMessageQueue(pingRequest)
    }

    protected fun finalize() {
        queueJob?.cancel()
        pingJob?.cancel()
        apiFlowThread.close()
        Logger.iLog(logTag, "Finalize class")
    }
}
