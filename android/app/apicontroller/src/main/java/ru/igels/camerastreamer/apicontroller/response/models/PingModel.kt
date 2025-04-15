package ru.igels.camerastreamer.apicontroller.response.models

internal data class PingModel(
    val messages: List<MessagesModel>?
) {
    data class MessagesModel(
        val message_id: Int,
        val message: String,
        val message_create_date: String
    )
}