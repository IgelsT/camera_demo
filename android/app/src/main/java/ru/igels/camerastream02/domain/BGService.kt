package ru.igels.camerastream02.domain

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import ru.igels.camerastream02.MainApp
import ru.igels.camerastream02.domain.logger.iLog


class BGService: Service() {
    private val logTag = "BGService"
    private val notification = AppNotification(this)
    companion object {
        private var instance: BGService? = null

        fun start() {
            if(instance != null) return
            sendCommand("start", null)
        }

        fun stop() {
            sendCommand("stop", null)
        }

        private fun <T> sendCommand(action: String, payload: T) {
            val context = MainApp.getContext()
            val service = Intent(context, BGService::class.java)
            service.action = action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service)
            } else {
                context.startService(service)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        iLog(logTag, "init")
        instance = this
        startForeground(1, notification.makeNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ""
        iLog(logTag, "onStartCommand $action")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        iLog(logTag, "BGService onTaskRemoved")
    }

    override fun onLowMemory() {
        iLog(logTag, "low memory")
    }

    override fun onTrimMemory(level: Int) {
        iLog(logTag, "trim memory $level")
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        iLog(logTag, "BGService destroyed")
        super.onDestroy()
    }



}
