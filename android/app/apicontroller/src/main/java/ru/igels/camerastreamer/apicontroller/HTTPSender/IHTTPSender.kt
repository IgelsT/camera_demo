package ru.igels.camerastreamer.apicontroller.HTTPSender

import ru.igels.camerastreamer.apicontroller.request.ApiRequest
import ru.igels.camerastreamer.apicontroller.response.ApiResponse

internal interface IHTTPSender {
    fun sendRequest(apiRequest: ApiRequest): ApiResponse
}