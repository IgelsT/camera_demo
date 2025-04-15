package ru.igels.camerastream02.domain

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import ru.igels.camerastream02.data.DevicePowerStateData
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.utilities.Utils

class PowerController private constructor(context: Context) {

    private val logTag = "PowerController"
    private val powerManager: PowerManager
    private val wifiManager: WifiManager
    private val powerWakeLock: PowerManager.WakeLock
    private val wifiWakeLock: WifiManager.WifiLock
    private val powerHandler: Handler = Utils.getThread("PowerThread")
    private val hiBatLevel = 95
    private val lowBatLevel = 85
    private var discharge = false
    private val wakeupTime = 180

    companion object {
        private var instance: PowerController? = null

        fun init(appContext: Context): PowerController {
            if (instance == null) instance = PowerController(appContext)
            return instance!!
        }

        fun getInstance(): PowerController {
            checkIsInit()
            return instance!!
        }

        private fun checkIsInit() {
            if (instance == null) throw Exception("init() PowerController first!")
        }

        fun fullPower(value: Boolean) {
            checkIsInit()
            if (value) instance!!.powerWakeLock.acquire()
            else instance!!.powerWakeLock.release()
        }

        fun wakeUpScreen(sec: Int) {
            checkIsInit()
            instance!!.wakeUpScreen(sec)
        }
    }

    init {
        iLog(logTag, "init")
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerWakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "myApp:notificationLock"
        )
        powerWakeLock.acquire()

//        val wakeLock =
//            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
//                    acquire()
//                }
//            }

        wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiWakeLock = wifiManager.createWifiLock(
            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            "myApp:wifiLock"
        )
        wifiWakeLock.acquire();
        accSave()
    }

    private fun wakeUpScreen(sec: Int) {
        iLog(logTag, "Wake up screen request for $sec sec.")
        val isScreenOn =
            if (Build.VERSION.SDK_INT >= 20) powerManager.isInteractive else powerManager.isScreenOn
        if (!isScreenOn) {
            val wl = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "myApp:wakeUpScreen"
            )
            wl.acquire(sec * 1000L)
        }
    }

    private fun accSave() {
        val settings = SettingsData.getInstance().settings
        val batLevel = DevicePowerStateData.getPowerState()
        if (!settings.accSave) {
            discharge = false
            powerHandler.postDelayed({ accSave() }, wakeupTime * 1000L)
            return
        }
        if (batLevel >= hiBatLevel) discharge = true
        else if (discharge && batLevel <= lowBatLevel) discharge = false

        if (discharge) {
            iLog(logTag, "Turn on screen for battery discharge")
            wakeUpScreen(wakeupTime)
        }
        powerHandler.postDelayed({ accSave() }, wakeupTime * 1000L)
    }

    protected fun finalize() {
        powerWakeLock.release()
        wifiWakeLock.release()
        Utils.closeThread(powerHandler)
        iLog(logTag, "Finalize cameraController class")
    }
}