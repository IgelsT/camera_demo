//package ru.igels.camerastream02.network.notneed.wsclient
//
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import com.google.gson.JsonPrimitive
//import ru.igels.camerastream02.data.AndroidInfoData
//import ru.igels.camerastream02.data.appsettings.SettingsData
//import ru.igels.camerastream02.camera.models.CameraInfoModel
//import ru.igels.camerastream02.network.models.DeviceInfoModel
//import ru.igels.camerastream02.utilites.Utils
//
//data class DeviceInfo(
//    val device_name: String,
//    val device_description: String,
//    val device_info: DeviceInfoModel
//)
//
//object PacketTypes {
//    val UNKNOWN = 0
//    val REQUEST = 100
//    val RESPONSE = 200
//    val REQUEST_ACTION = 300
//    val INFO = 400
//}
//
//open class BasePacket {
//    @Transient
//    open val type: Int = 0
//
//    @Transient
//    open val action: String = ""
//    var messageid: Int = 0
//}
//
//class Auth(
//    var user_name: String = "",
//    var user_pass: String = "",
//    var user_type: Int = 0,
//    var device_id: String = "",
//)
//
//class Result(
//    var success: Boolean = false,
//    var reason: String = "",
//    var code: Int = 0,
//) {
//    companion object {
//        fun fromJSON(result: JsonObject): Result {
//            try {
//                return Gson().fromJson(result, Result::class.java)
//            } catch (e: Exception) {
//                return Result()
//            }
//        }
//    }
//}
//
//data class ToServerRequestModel(
//    override val action: String,
//    val auth: Auth,
//    val data: JsonObject? = null
//) : BasePacket() {
//    override val type = PacketTypes.REQUEST
//
//}
//
//data class ToServerResponseModel(
//    override val action: String,
//    val auth: Auth,
//    val result: Result,
//    val data: JsonObject? = null
//) : BasePacket() {
//    override val type = PacketTypes.RESPONSE
//}
//
//data class FromServerResponseModel(
//    override val action: String,
//    val result: Result,
//    val data: JsonObject? = null
//) : BasePacket() {
//    override val type = PacketTypes.RESPONSE
//}
//
//data class FromServerRequestModel(override val action: String, val data: JsonObject? = null) :
//    BasePacket() {
//    override val type = PacketTypes.REQUEST
//}
//
//data class FromServerInfoModel(val data: JsonObject? = null) : BasePacket() {
//    override val type = PacketTypes.INFO
//}
//
//fun <T> makeData(src: T): JsonObject {
//    val jsonStr = Gson().toJson(src)
//    return Gson().fromJson(jsonStr, JsonObject::class.java)
//}
//
//private fun makeAuth(): Auth {
//    val settings = SettingsData.getSettings()
//    return Auth(
//        user_name = settings.userName,
//        user_pass = Utils.getMD5String(settings.userPassword),
//        user_type = 1,
//        device_id = AndroidInfoData.getAndroidID()
//    )
//}
//
//fun makeRequest(action: String, data: JsonObject? = JsonObject()): BasePacket {
//    return ToServerRequestModel(
//        action = action,
//        auth = makeAuth(),
//        data = data
//    )
//}
//
//fun makeResponse(
//    action: String,
//    result: Result,
//    data: JsonObject? = JsonObject()
//): BasePacket {
//    return ToServerResponseModel(
//        action = action,
//        auth = makeAuth(),
//        result = result,
//        data = data
//    )
//}
//
//fun <T> getParam(obj: JsonObject, name: String, def: T): T {
//    try {
//        val field = obj.get(name)
//        if (field is JsonPrimitive) {
//            if (field.isBoolean) return field.asBoolean as T
//            if (field.isNumber) return field.asInt as T
//            if (field.isString) return field.asString as T
//        } else if (field is JsonObject) {
//            return field as T
//        }
//    } catch (e: java.lang.Exception) {
//    }
//    return def
//}
//
//fun inPacketBuilder(jsonObj: JsonObject): BasePacket {
//    val type = getParam(jsonObj, "type", PacketTypes.UNKNOWN)
//    var packet = BasePacket()
//    when (type) {
//        PacketTypes.REQUEST ->
//            packet = FromServerRequestModel(
//                action = getParam(jsonObj, "action", ""),
//                data = getParam(jsonObj, "data", JsonObject()),
//            )
//        PacketTypes.RESPONSE ->
//            packet = FromServerResponseModel(
//                action = getParam(jsonObj, "action", ""),
//                result = Result.fromJSON(getParam(jsonObj, "result", JsonObject())),
//                data = getParam(jsonObj, "data", JsonObject()),
//            )
//        PacketTypes.INFO -> {}
//        else -> {}
//    }
//    return packet
//}