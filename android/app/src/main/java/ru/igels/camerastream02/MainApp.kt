package ru.igels.camerastream02

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.multidex.MultiDex
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.data.*
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.*
import ru.igels.camerastream02.domain.logger.ExternalLogger
import ru.igels.camerastream02.domain.logger.LoggerController
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.ui.MainActivity
import ru.igels.camerastreamer.apicontroller.ApiController
import ru.igels.camerastreamer.apicontroller.shared.ApiSettings
import java.lang.reflect.InvocationTargetException
import kotlin.system.exitProcess


class MainApp : Application() {

    private val logTag = "MainApp"
    private val appSettingsName = "ru.igels.camerastream"
    private val logPath = Environment.getExternalStorageDirectory().path + "/DCIM/BOT/"
    private lateinit var appContext: Context
    lateinit var mainActivityIntent: Intent

    companion object {
        private lateinit var instance: MainApp

        @JvmName("getInstanceFun")
        fun getInstance(): MainApp {
            return instance
        }

        fun getContext(): Context {
            return instance.appContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        instance = this
        MultiDex.install(applicationContext) // !!!Hack for Android 4.2
        setGlobalErrorHandler()

        AppMessageBus.init(applicationContext)
        PermissionData.init(applicationContext)
        LoggerController.init(logPath, appSettingsName)

        iLog(logTag, "Start main App")
        mainActivityIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val settingsData = SettingsData.init(appContext, appSettingsName, AppMessageBus.getInstance())
        val settings = settingsData.settings
        if (settings.isCrashed) {
            settingsData.setIsCrash(false)
        }
        DeviceNetworkStateData.init(applicationContext)
        DevicePowerStateData.init(applicationContext)
        DeviceLocationData.init(applicationContext)
        PowerController.init(applicationContext)
        setApiController()
        CameraController.init(applicationContext)
        BGService.start()
    }

    private fun setApiController() {
        val settings = SettingsData.getInstance().settings
        ApiController.init(
            ApiSettings(
                settings.baseUrl,
                settings.userName,
                settings.userPassword,
                settings.accountToken,
                settings.deviceToken,
                AndroidInfoData.getAndroidID()
            ), ExternalLogger
        )
    }

    private fun setGlobalErrorHandler() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            //Do your own error handling here
            val settingsData = SettingsData.getInstance()
            val processName = appContext.packageName;
            var message = paramThrowable.message ?: ""
            var stackTrace: Array<StackTraceElement> = paramThrowable.stackTrace
            val cause = paramThrowable.cause
            if (cause is InvocationTargetException) {
                message += " " + cause.targetException.message
                stackTrace = cause.targetException.stackTrace
            } else if (cause != null) {
                stackTrace = cause.stackTrace
            }
            for (line in stackTrace) {
                if (line.toString().contains(processName))
                    message += "\r\n" + line.toString()
            }
            eLog(logTag, message)
            settingsData.setIsCrash(true)
            LoggerController.markLastAsCrash()
            BGService.stop()
            paramThrowable.printStackTrace()
            if (oldHandler != null) oldHandler.uncaughtException(
                paramThread,
                paramThrowable
            ) //Delegates to Android's error handling
            exitProcess(2) //Prevents the service/app from freezing
        }
    }

    protected fun finalize() {
        iLog(logTag, "Finalize MainApp")
    }

    override fun onTerminate() {
        super.onTerminate()
        iLog(logTag, "Main App terminate")
    }
}