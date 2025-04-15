package ru.igels.camerastreamer.apicontroller.response.models

import ru.igels.camerastreamer.apicontroller.shared.API_MESSAGES
import java.util.Date

internal data class ApiMessageModel<T>(
    val messageId: Int,
    val createDate: Date,
    val action: API_MESSAGES,
    val data: T?
)
