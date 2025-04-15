package ru.igels.camerastreamer.apicontroller.HTTPSender

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import ru.igels.camerastreamer.apicontroller.request.ApiRequest
import ru.igels.camerastreamer.apicontroller.response.ApiResponse
import ru.igels.camerastreamer.apicontroller.response.ApiResponseFactory.createResponse


internal class HTTPSender : IHTTPSender {

    private val logTag = "HTTPSender"
    private var client: OkHttpClient = OkHttpClient()
    private val jsonMediaTypeUTF8 = MediaType.parse("application/json; charset=utf-8")
    private val jsonMediaType = MediaType.parse("application/json")

    override fun sendRequest(apiRequest: ApiRequest): ApiResponse {
        try {
            val requestBuilder = Request.Builder().url(apiRequest.url)
            if (apiRequest.authToken != "") requestBuilder.addHeader("Authorization", apiRequest.authToken)
            if (apiRequest.file == null) {
                val jsonBody = RequestBody.create(jsonMediaTypeUTF8, apiRequest.getBodyJSONString());
                requestBuilder.post(jsonBody)
            } else {
                val postBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("data", apiRequest.getBodyJSONString())
                    .addFormDataPart(
                        "file", apiRequest.file!!.file.name,
                        RequestBody.create(
                            MediaType.parse(apiRequest.file!!.mediaType),
                            apiRequest.file!!.file
                        )
                    ).build()
                requestBuilder.post(postBody)
            }

            val request = requestBuilder.build()
            val response: Response = client.newCall(request).execute()
            val body = response.body()
            return createResponse(response.code(), apiRequest.action, body?.string())
        } catch (e: Exception) {
            var message = "Network Error"
            if (e.message != null) message = e.message!!;
            return createResponse(0, apiRequest.action, errorStr = message)
        }
    }
}