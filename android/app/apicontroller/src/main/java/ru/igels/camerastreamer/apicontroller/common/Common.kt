package ru.igels.camerastreamer.apicontroller.common

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MediaType
import ru.igels.camerastreamer.shared.logger.ILogger
import ru.igels.camerastreamer.apicontroller.shared.apiSettings
import java.math.BigInteger
import java.security.MessageDigest

internal object API_END_POINTS {
    const val auth = "auth"
    const val device = "device"
    const val files = "files"
    const val test = "test"
}

internal object API_ACTIONS {
    const val UNKNOWN = "UNKNOWN"
    const val LOGIN = "loginDevice"
    const val SET_DEVICE_INFO = "setDeviceInfo"
    const val SET_DEVICE_STATE = "setDeviceState"
    const val SET_CAMERA_LIST = "setCameraList"
    const val SEND_LOG = "sendLog"
    const val SET_LOG_LIST = "setLogList"
    const val PING = "ping"
    const val APPLIED_MESSAGES = "appliedMessages"
    const val EXECUTED_MESSAGES = "executedMessages"
}

internal object Logger {
    var externalLogger: ILogger? = null

    fun eLog(description: String, message: String, trace: String? = null) {
        if ((externalLogger?.logLevel ?: 0) > 0)
            externalLogger?.eLog(description, message, trace)
    }

    fun iLog(description: String, message: String) {
        if ((externalLogger?.logLevel ?: 0) > 1)
            externalLogger?.iLog(description, message)
    }

    fun dLog(description: String, message: String) {
        if ((externalLogger?.logLevel ?: 0) > 2)
            externalLogger?.dLog(description, message)
    }
}

internal object Utils {
    val jsonMediaTypeUTF8 = MediaType.parse("application/json; charset=utf-8")
    val jsonMediaType = MediaType.parse("application/json")

    val apiToken: String
        get() = if (apiSettings.deviceToken != "") apiSettings.deviceToken else apiSettings.userToken

    fun getApiUrl(endpoint: String, action: String) = "http://${apiSettings.url}/api/v1/$endpoint/$action"

    fun getMD5String(rawString: String): String {
        val crypt = MessageDigest.getInstance("MD5");
        crypt.update(rawString.toByteArray());
        return BigInteger(1, crypt.digest()).toString(16)
    }

    fun string2JSON(str: String): JsonObject? {
        return try {
            Gson().fromJson(str, JsonObject::class.java)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> enumFromString(value: String): T where T : Enum<T> {
        return enumValues<T>().find { it.toString() == value } ?: enumValues<T>().first()
    }

    inline fun <reified T> objectFromJson(data: JsonElement?): T? {
        return try {
            Gson().fromJson(data, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

//    fun <T> getParam(obj: JsonObject, name: String, def: T): T {
//        try {
//            val field = obj.get(name)
//            if (field is JsonPrimitive) {
//                if (field.isBoolean) return field.asBoolean as T
//                if (field.isNumber) return field.asInt as T
//                if (field.isString) return field.asString as T
//            } else if (field is JsonObject) {
//                return field as T
//            } else if (field is JsonArray) {
//                return field as T
//            }
//        } catch (e: Exception) {
//            eLog(logTag, "error get $name from json")
//        }
//        return def
//    }

    //        val myClassName = ApiResponseLogin::class.java
//        val dd = myClassName.getDeclaredConstructor(ApiResponseBase::class.java)
//            .newInstance(response);

//    fun changeSettings(data: JsonObject): Boolean {
//        try {
//            val currentSettings = SettingsData.getSettings()
//            val currentState = SettingsData.getAppState()
//            currentSettings.deviceName = getParam(data, "device_name", currentSettings.deviceName)
//            currentSettings.deviceDescription =
//                getParam(data, "device_description", currentSettings.deviceDescription)
//            currentSettings.cameraID = getParam(data, "device_camera_id", currentSettings.cameraID)
//            currentSettings.focus = SettingsData.getFocusFromString(
//                getParam(
//                    data, "device_focus", currentSettings.focus.toString()
//                )
//            )
//            val res = getParam(
//                data,
//                "device_resolution",
//                "${currentSettings.width}x${currentSettings.height}"
//            ).split("x")
//            currentSettings.width = res[0].toInt()
//            currentSettings.height = res[1].toInt()
//            currentSettings.fps = getParam(data, "device_fps", currentSettings.fps)
//            currentSettings.rotation =
//                SettingsData.orientationFromEnum(getParam(data, "device_orientation", "TOP"))
//            currentSettings.quality = getParam(data, "device_quality", currentSettings.quality)
//            val isStream =
//                getParam(data, "device_status", (if (currentState.isStream) 1 else 0)) == 1
//            SettingsData.saveSettings(currentSettings)
//            CameraController.setStream(isStream)
//        } catch (e: Exception) {
//            eLog(logTag, "error set settings from api $e")
//            return false
//        }
//        return true
//    }
}