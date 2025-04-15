package ru.igels.camerastream02.appmessagebus

import android.content.Context
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.igels.camerastream02.domain.models.AppMessage

class AppMessageBus : IAppMessageBus {
    private val logTag: String = "AppMessageBus"

    private val appMessageFlow =
        MutableSharedFlow<AppMessage<*>>(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    companion object {
        private var instance: AppMessageBus? = null

        fun init(context: Context): AppMessageBus {
            if (instance == null) instance = AppMessageBus()
            return instance!!
        }

        fun getInstance(): AppMessageBus {
            if (instance == null) throw Exception("init() AppMessageBus first!")
            return instance as AppMessageBus
        }

        fun getAppMessageBusFlow(): SharedFlow<AppMessage<*>> = getInstance().getAppMessageBusFlow()
        fun publish(event: AppMessage<*>) = getInstance().publish(event)
    }


    override fun getAppMessageBusFlow(): SharedFlow<AppMessage<*>> {
        return appMessageFlow.asSharedFlow()
    }

    override fun publish(event: AppMessage<*>) {
        appMessageFlow.tryEmit(event)
    }
}