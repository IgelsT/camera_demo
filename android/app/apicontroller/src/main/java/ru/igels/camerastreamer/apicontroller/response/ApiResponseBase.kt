package ru.igels.camerastreamer.apicontroller.response

import com.google.gson.Gson
import com.google.gson.JsonElement
import ru.igels.camerastreamer.apicontroller.shared.API_ERROR_CODES

internal data class ApiResponseBase(
    val result: String = "error",
    val code: Int = 0,
    val action: String = "",
    val data: JsonElement? = null,
    var error: ApiResponseError? = null,
) {
    companion object {
        fun fromJSON(
            code: Int,
            action: String,
            jsonStr: String?,
            customError: String? = null
        ): ApiResponseBase {
            var response: ApiResponseBase
            try {
                response = Gson().fromJson(jsonStr, ApiResponseBase::class.java)
                response.error?.let { it.fromApi = true }
            } catch (e: Exception) {
                response = ApiResponseBase(
                    code = code,
                    action = action,
                    error = ApiResponseError(
                        code = if (customError != null) API_ERROR_CODES.NETWORK_ERROR else API_ERROR_CODES.RESPONSE_JSON_PARSE_ERROR,
                        message = customError ?: (e.message ?: "Error parse json"),
                        fromApi = false
                    )
                )
            }
            return response
        }
    }
}