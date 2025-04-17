package ru.igels.camerastreamer.apicontroller.request

import com.google.gson.Gson
import com.google.gson.JsonObject
import ru.igels.camerastreamer.apicontroller.response.ApiResponse
import ru.igels.camerastreamer.apicontroller.common.API_ACTIONS
import ru.igels.camerastreamer.apicontroller.common.API_END_POINTS
import ru.igels.camerastreamer.apicontroller.common.Utils
import ru.igels.camerastreamer.apicontroller.common.Utils.getMD5String
import ru.igels.camerastreamer.apicontroller.shared.apiSettings
import ru.igels.camerastreamer.shared.models.DeviceInfoModel
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelOUT
import ru.igels.camerastreamer.apicontroller.request.models.AppliedMessagesModel
import ru.igels.camerastreamer.apicontroller.request.models.CameraListRequestModel
import ru.igels.camerastreamer.apicontroller.request.models.InfoRequestModel
import ru.igels.camerastreamer.apicontroller.request.models.LogListRequestModel
import ru.igels.camerastreamer.apicontroller.request.models.LoginRequestModel
import ru.igels.camerastreamer.apicontroller.request.models.StateRequestModel
import ru.igels.camerastreamer.shared.models.CameraInfoModel
import java.io.File

internal data class ApiRequestFile(val file: File, val mediaType: String)


internal sealed class ApiRequest(
    val url: String,
    val action: String,
    var file: ApiRequestFile? = null,
    var doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) {
    val logTag = "HTTPApiRequest"

    val authToken: String = Utils.apiToken
    open var data: Any = object {}

    fun getBodyJSON(): JsonObject {
        val body = JsonObject()
        body.add("data", Gson().toJsonTree(data))
        return body
    }

    fun getBodyJSONString(): String = getBodyJSON().toString()
}

internal class ApiRequestLogin(
    data: LoginRequestModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.auth, API_ACTIONS.LOGIN),
    action = API_ACTIONS.LOGIN,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestSetInfo(
    data: InfoRequestModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.SET_DEVICE_INFO),
    action = API_ACTIONS.SET_DEVICE_INFO,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestSetState(
    data: StateRequestModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.SET_DEVICE_STATE),
    action = API_ACTIONS.SET_DEVICE_STATE,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestSetCamList(
    data: CameraListRequestModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.SET_CAMERA_LIST),
    action = API_ACTIONS.SET_CAMERA_LIST,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestSetLogList(
    data: LogListRequestModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.SET_LOG_LIST),
    action = API_ACTIONS.SET_LOG_LIST,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestPing(
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.PING),
    action = API_ACTIONS.PING,
    doneCallback = doneCallback
)

internal class ApiRequestAppliedMessages(
    data: AppliedMessagesModel,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.PING),
    action = API_ACTIONS.APPLIED_MESSAGES,
    doneCallback = doneCallback
) {
    override var data: Any = data
}

internal class ApiRequestCompleteCommand(
    data: Int,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.EXECUTED_MESSAGES),
    action = API_ACTIONS.EXECUTED_MESSAGES,
    doneCallback = doneCallback
) {
    override var data: Any = object {
        val message_id = data
    }
}

internal class ApiRequestLogFile(
    file: ApiRequestFile,
    doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null
) : ApiRequest(
    url = Utils.getApiUrl(API_END_POINTS.device, API_ACTIONS.SEND_LOG),
    action = API_ACTIONS.SEND_LOG,
    file = file,
    doneCallback = doneCallback
)

internal object ApiRequestFactory {

    val logTag = "ApiRequestFactory"

    fun loginRequest(doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null): ApiRequestLogin {
        return ApiRequestLogin(
            data = LoginRequestModel(
                apiSettings.userName,
                getMD5String(apiSettings.userPassword),
                apiSettings.deviceUid
            ),
            doneCallback = doneCallback
        )
    }

    fun deviceInfoRequest(info: DeviceInfoModel): ApiRequest {
        return ApiRequestSetInfo(
            data = InfoRequestModel(
                info = info
            )
        )
    }

    fun deviceStateRequest(state: DeviceStateModelOUT): ApiRequest =
        ApiRequestSetState(StateRequestModel(state))

    fun deviceCameraListRequest(list: List<CameraInfoModel>): ApiRequest =
        ApiRequestSetCamList(CameraListRequestModel(list))

    fun logListRequest(list: List<String>): ApiRequest =
        ApiRequestSetLogList(LogListRequestModel(list))

    fun pingRequest(doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null): ApiRequest =
        ApiRequestPing(doneCallback)

    fun appliedMessagesRequest(msgList: List<Int>): ApiRequest =
        ApiRequestAppliedMessages(AppliedMessagesModel(msgList))

    fun completeCommandRequest(id: Int): ApiRequest = ApiRequestCompleteCommand(id)

    fun logFileRequest(file: File): ApiRequest = ApiRequestLogFile(ApiRequestFile(file, ""))

//    private fun fileLastCrashSend(): ApiRequest? {
//        val filename = LoggerController.getLastCrash() ?: return null
//        val file = File(filename)
//        val data = JsonObject()
//        data.addProperty("file", file.name)
//        val body = makeBodyJson("sendLog", data)
//        return ApiRequest(
//            EndPoints.files,
//            body,
//            getDeviceToken(),
//            file = ApiRequestFile(file, "application/zip")
//        )
//    }
//
//    fun fileLastCrashSReceive(response: ApiResponse): ApiRequest? {
//        // Expected a com.google.gson.JsonArray but was com.google.gson.JsonObject
//        val data = Gson().fromJson(response.data, JsonElement::class.java)
//        iLog(logTag, "get fileLastCrashSReceive response $data")
//        if(data is JsonArray) {
//            for (file in data) {
//                LoggerController.deleteLog(file.asString)
//            }
//        }
//        return fileLastCrashSend()
//    }

    //
//    fun testRequest(doneCallback: ((ApiRequest, ApiResponse) -> Unit)? = null): ApiRequest {
//        return makeBaseRequest(API_END_POINTS.test, "", doneCallback)
//    }
}


