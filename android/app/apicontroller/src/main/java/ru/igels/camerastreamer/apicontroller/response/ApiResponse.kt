package ru.igels.camerastreamer.apicontroller.response

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import ru.igels.camerastreamer.apicontroller.common.API_ACTIONS
import ru.igels.camerastreamer.apicontroller.common.*
import ru.igels.camerastreamer.apicontroller.common.Utils
import ru.igels.camerastreamer.apicontroller.response.models.LoginModel
import ru.igels.camerastreamer.apicontroller.response.models.MessageSettingsModel
import ru.igels.camerastreamer.apicontroller.response.models.ApiMessageModel
import ru.igels.camerastreamer.apicontroller.response.models.FileRequestModel
import ru.igels.camerastreamer.apicontroller.response.models.PingModel
import ru.igels.camerastreamer.apicontroller.shared.API_ERROR_CODES
import ru.igels.camerastreamer.apicontroller.shared.API_MESSAGES
import java.text.SimpleDateFormat

internal class ApiResponseError(
    var code: API_ERROR_CODES,
    var message: String = "",
    var reason: String = "",
    var fromApi: Boolean = false
)


internal sealed class ApiResponse(responseBase: ApiResponseBase? = null) {

    var success: Boolean = responseBase?.result == "ok"
    val code: Int = responseBase?.code ?: 0
    val action: String = responseBase?.action ?: API_ACTIONS.UNKNOWN
    var error: ApiResponseError? = responseBase?.error

    inline fun <reified T> feelData(data: JsonElement?): T? {
//        val data1 = Gson().toJson(data)
        return try {
            Gson().fromJson(data, T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

internal class ApiResponseBlank(responseBase: ApiResponseBase? = null) : ApiResponse(responseBase)

internal class ApiResponseLogin(responseBase: ApiResponseBase) : ApiResponse(responseBase) {
    val data: LoginModel? = feelData<LoginModel>(responseBase.data)
}

@SuppressLint("SimpleDateFormat")
internal class ApiResponsePing(responseBase: ApiResponseBase) : ApiResponse(responseBase) {
    //    val messages = feelData<List<ApiMessageModel<*>>>(responseBase.data)
    val data = feelData<PingModel>(responseBase.data)
}

internal object ApiResponseFactory {

    const val logTag = "ApiResponseFactory"

    fun createResponse(
        code: Int,
        action: String,
        jsonStr: String? = null,
        errorStr: String? = null
    ): ApiResponse {
        try {
            val responseBase = ApiResponseBase.fromJSON(code, action, jsonStr, errorStr)
            return responseToModel(responseBase)

        } catch (e: Exception) {
            Logger.eLog(logTag, e.message.toString())
            return ApiResponseBlank()
        }
    }

    private fun responseToModel(response: ApiResponseBase): ApiResponse {
        return when (response.action) {
            API_ACTIONS.LOGIN -> ApiResponseLogin(response)
            API_ACTIONS.PING -> ApiResponsePing(response)
            else -> ApiResponseBlank()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun analizeApiMessage(msg: PingModel.MessagesModel): ApiMessageModel<*>? {
        val data = Utils.string2JSON(msg.message) ?: return null

        val msgAction = API_MESSAGES.values().find {
            it.code == data.get("action")?.asString
        } ?: API_MESSAGES.UNKNOWN
        val id = msg.message_id
        val created = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .parse(msg.message_create_date) ?: java.util.Date()


        val msgData = when (msgAction) {
            API_MESSAGES.SETTINGS ->
                Utils.objectFromJson<MessageSettingsModel>(data.get("data"))?.toOUT()

            API_MESSAGES.LOG_FILE ->
                Utils.objectFromJson<FileRequestModel>(data.get("data"))?.file_name

            else -> null
        }

        return ApiMessageModel(id, created, msgAction, msgData)
    }
}


//    fun appliedMessagesReceive(response: ApiResponse): API_PACKET_ANALIZE_RESULT {
//        Logger.iLog(logTag, "get appliedMessagesReceive response")
//        return API_PACKET_ANALIZE_RESULT.ACCEPT
//    }
//
