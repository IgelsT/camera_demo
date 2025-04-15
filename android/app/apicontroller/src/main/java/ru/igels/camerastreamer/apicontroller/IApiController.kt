package ru.igles.camerastreamer.apicontroller

import kotlinx.coroutines.flow.SharedFlow
import ru.igels.camerastreamer.apicontroller.ApiController
import ru.igels.camerastreamer.shared.logger.ILogger
import ru.igels.camerastreamer.apicontroller.shared.ApiSettings
import ru.igels.camerastreamer.apicontroller.shared.ApiStatus
import ru.igels.camerastreamer.shared.models.DeviceInfoModel
import ru.igels.camerastreamer.apicontroller.shared.DeviceStateModelOUT
import ru.igels.camerastreamer.shared.models.CameraInfoModel

interface IApiController {
    fun init(settings: ApiSettings, logger: ILogger? = null): ApiController
    fun setSettings(settings: ApiSettings)
    fun getStatusFlow(): SharedFlow<ApiStatus>
    suspend fun manualLogin(apiUrl: String, userName: String, userPassword: String): ApiStatus
    fun logout()
    fun sendDeviceInfo(info: DeviceInfoModel)
    fun sendDeviceState(state: DeviceStateModelOUT)
    fun sendDeviceCameraList(camList: List<CameraInfoModel>)
}