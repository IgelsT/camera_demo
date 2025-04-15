package ru.igels.camerastreamer.camerav1.common

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import ru.igels.camerastreamer.shared.logger.ILogger

internal object Logger {
    var externalLogger: ILogger? = null

    fun eLog(description: String, message: String, trace: String? = null) {
        if ((externalLogger?.logLevel ?: 0) > 0)
            externalLogger?.eLog(description, message, trace)
    }

    fun iLog(description: String, message: String) {
        if ((externalLogger?.logLevel ?: 0) > 1)
            externalLogger?.iLog(description, message)
    }

    fun dLog(description: String, message: String) {
        if ((externalLogger?.logLevel ?: 0) > 2)
            externalLogger?.dLog(description, message)
    }
}

internal object Utils {
    fun closeThread(handler: Handler): Boolean {
        return try {
            looperQuit(handler)
            handler.removeCallbacksAndMessages(null);
            handler.looper.thread.join()
            true
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    private fun looperQuit(handler: Handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) handler.looper.quitSafely()
        else handler.looper.quit()
    }

    fun getThread(name: String): Handler {
        val backgroundThread = HandlerThread(name)
        backgroundThread.start()
        return Handler(backgroundThread.looper)
    }
}