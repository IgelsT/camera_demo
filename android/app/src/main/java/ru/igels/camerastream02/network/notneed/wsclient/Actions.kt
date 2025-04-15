//package ru.igels.camerastream02.network.notneed.wsclient
//
//import android.content.Context
//import android.os.PowerManager
//import com.google.gson.JsonObject
//import ru.igels.camerastream02.MainApp
//import ru.igels.camerastream02.camera.models.CAMFOCUSES
//import ru.igels.camerastream02.data.AndroidInfoData
//import ru.igels.camerastream02.data.DevicePowerStateData
//import ru.igels.camerastream02.data.appsettings.SettingsData
//import ru.igels.camerastream02.domain.LoggerController
//import ru.igels.camerastream02.domain.eLog
//import ru.igels.camerastream02.domain.iLog
//import ru.igels.camerastream02.network.models.CameraList
//import ru.igels.camerastream02.network.models.FileUpload
//
//object Actions {
//
//    val logTag = "Actions"
//
//    fun login(packet: BasePacket) {
//        if (packet is FromServerResponseModel && packet.result.success) {
//            val data = packet.data ?: JsonObject()
//            SettingsData.userToken = getParam(data, "token", "")
//            sendDeviceInfo()
////            sendDeviceState()
//            sendCamerasList()
//        }
//    }
//
//    fun sendDeviceInfo(packet: BasePacket? = null) {
//        if (packet == null) {
//            val deviceInfo = AndroidInfoData.buildDeviceInfo()
//            val settings = SettingsData.getSettings()
//            WSClient.addToMessageQueue(
//                makeRequest(
//                    action = "deviceInfo",
//                    data = makeData(
//                        DeviceInfo(
//                            settings.deviceName,
//                            settings.deviceDescription,
//                            deviceInfo
//                        )
//                    )
//                )
//            )
//        }
//    }
//
////    fun sendDeviceState(packet: BasePacket? = null) {
////        if (packet == null) {
////            val settings = SettingsData.getSettings()
////            val state = SettingsData.getAppState()
////            val info = DeviceStateModel(
////                device_uid = AndroidInfoData.getAndroidID(),
////                device_name = settings.deviceName,
////                device_description = settings.deviceDescription,
////                device_camera_id = settings.cameraID,
////                device_focus = settings.focus.toString(),
////                device_resolution = "${settings.width}x${settings.height}",
////                device_orientation = SettingsData.orientationToEnum(),
////                device_fps = settings.fps,
////                device_quality = settings.quality,
////                device_status = if (state.isStream) 1 else 0,
////                device_power = DevicePowerStateData.getPowerState(),
////                device_location = ""
////            )
////            WSClient.addToMessageQueue(makeRequest(action = "deviceState", data = makeData(info)))
////        }
////    }
//
//    fun sendCamerasList(packet: BasePacket? = null) {
//        if (packet == null) {
//            val list = SettingsData.getCameraList()
//            if (list.isNotEmpty()) {
//                val packet = makeRequest(
//                    action = "camerasList",
//                    data = makeData(CameraList(list))
//                )
//                WSClient.addToMessageQueue(packet)
//            }
//        }
//    }
//
//    fun setParams(packet: BasePacket) {
//        try {
//            val settings = SettingsData.getSettings()
//            val state = SettingsData.getAppState()
//            val data = (packet as FromServerRequestModel).data ?: JsonObject()
//
//            val deviceName = getParam(data, "device_name", "")
//            settings.deviceName = deviceName
//
//            val deviceDescr = getParam(data, "device_description", "")
//            settings.deviceDescription = deviceDescr
//
//            val cameraID = getParam(data, "device_camera_id", -1)
//            // Check if cam in available list
//            if (cameraID < 0 || cameraID > 2) return
//            settings.cameraID = cameraID
//
//            val focus = getParam(data, "device_focus", "")
//            // Check if focus in available list
////        if (cameraID < 0 || cameraID > 2) return
//            settings.focus = CAMFOCUSES.valueOf(focus)
//
//            val resolution = getParam(data, "device_resolution", "0x0")
//            // Check if cam in available list
//            val w_h = resolution.split("x")
//            if (w_h[0].toInt() == 0 || w_h[1].toInt() == 0) return
//            settings.width = w_h[0].toInt()
//            settings.height = w_h[1].toInt()
//
//            val rotation = getParam(data, "device_orientation", -1)
//            if (rotation != 0 && rotation != 90 && rotation != 180 && rotation != 270) return
//            settings.rotation = rotation
//
//            val fps = getParam(data, "device_fps", -1)
//            if (fps != 5 && fps != 10 && fps != 15 && fps != 20 && fps != 25) return
//            settings.fps = fps
//
//            val quality = getParam(data, "device_quality", -1)
//            if (quality < 0 || quality > 4) return
//            settings.quality = quality
//
//            val status = getParam(data, "device_status", -1)
//            if (status < 0 || status > 1) return
//            state.isStream = status != 0
//
//            SettingsData.saveSettings(settings)
//        } catch (e: Exception) {
//            eLog(logTag, "setParams failed ${e.message}")
//        }
//    }
//
//    fun ping(packet: BasePacket? = null) {
//        if (packet == null) {
//            val pkt = makeRequest(
//                action = "ping",
//            )
//            WSClient.addToMessageQueue(pkt)
//        } else {
//
//        }
//    }
//
//    fun restartApp() {
//        iLog(logTag, "Try restart app")
//    }
//
//    fun deviceReboot() {
//        iLog(logTag, "Try reboot device")
//        val ctx = MainApp.getInstance().applicationContext
//        val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
//        powerManager.reboot(null)
//    }
//
//    fun requestLog(packet: BasePacket? = null) {
//        iLog(logTag, "Log requested, try send to server")
//////        val filename = LoggerController.getArchiveLog()
////        if(filename == "") return
////        val data = (packet as FromServerRequestModel).data ?: JsonObject()
////        val settings = SettingsData.getSettings()
////
////        val url = getParam(data, "uploadUrl", "http://" + settings.baseUrl + "/api/upload")
////        if(url == "") return
////        WSClient.addToFileQueue(FileUpload(url, filename))
//    }
//}