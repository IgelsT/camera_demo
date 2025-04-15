package ru.igels.camerastream02.utilities

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import java.math.BigInteger
import java.security.MessageDigest


object Utils {

    private fun looperQuit(handler: Handler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) handler.looper.quitSafely()
        else handler.looper.quit()
    }

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

    fun getThread(name: String): Handler {
        val backgroundThread = HandlerThread(name)
        backgroundThread.start()
        return Handler(backgroundThread.looper)
    }

    fun resetThread(handler: Handler): Boolean {
        try {
            looperQuit(handler)
            handler.looper.thread.stop()
        } catch (e: Exception) {
        }
        try {
            handler.looper.thread.start()
        }
        catch (e: Exception) {
            return false
        }
        return true
    }

    fun getMD5String(rawString: String): String {
        val crypt = MessageDigest.getInstance("MD5");
        crypt.update(rawString.toByteArray());
        return BigInteger(1, crypt.digest()).toString(16)
    }

}