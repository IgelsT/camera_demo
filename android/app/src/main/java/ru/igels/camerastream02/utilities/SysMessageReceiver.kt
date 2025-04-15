package ru.igels.camerastream02.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.igels.camerastream02.domain.logger.iLog

class SysMessageReceiver : BroadcastReceiver() {
    val logTag = "Receiver"

    override fun onReceive(context: Context, intent: Intent) {
        iLog(logTag, "System message receive ${intent.toString()}")
    }
}