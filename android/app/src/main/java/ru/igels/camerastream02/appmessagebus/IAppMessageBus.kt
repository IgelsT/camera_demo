package ru.igels.camerastream02.appmessagebus

import kotlinx.coroutines.flow.SharedFlow
import ru.igels.camerastream02.domain.models.AppMessage

interface IAppMessageBus {
    fun getAppMessageBusFlow(): SharedFlow<AppMessage<*>>
    fun publish(event: AppMessage<*>)
}