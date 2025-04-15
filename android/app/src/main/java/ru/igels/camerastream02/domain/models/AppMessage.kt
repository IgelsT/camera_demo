package ru.igels.camerastream02.domain.models

data class AppMessage<T>(val type: APPMSG_TYPE, val payload: T)
