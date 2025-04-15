package ru.igels.camerastream02.data

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppMessage

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class DevicePowerStateData private constructor(context: Context) {
    private var batteryStatus: Intent?
    val logTag = "DevicePowerStateData"
    var isCharging = false
    var batteryLevel = 0
    var powState = 0

    companion object {
        private var instance: DevicePowerStateData? = null

        fun init(context: Context): DevicePowerStateData {
            if (instance == null) instance = DevicePowerStateData(context)
            return instance!!
        }

        fun getInstance(): DevicePowerStateData {
            checkIsInit()
            return instance!!
        }

        private fun checkIsInit() {
            if (instance == null) throw Exception("init() DevicePowerStateData first!")
        }

        fun getPowerState(): Int {
            checkIsInit()
            return if (instance!!.isCharging) instance!!.batteryLevel
            else instance!!.batteryLevel * -1
        }
    }

    init {
        iLog(logTag, "init")
        IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            batteryStatus = context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    batteryLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
                    val chargePlug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val powerState = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = if (chargePlug != 0) true
                    else {
                        powerState == BatteryManager.BATTERY_STATUS_CHARGING || powerState == BatteryManager.BATTERY_STATUS_FULL;
                    }
                    if (powState != getPowerState()) {
                        powState = getPowerState()
                        AppMessageBus.publish(
                            AppMessage(APPMSG_TYPE.POWERSTATE_UPDATED, powState)
                        )
                    }
                    /**
                    BATTERY_STATUS_UNKNOWN 1
                    BATTERY_STATUS_CHARGING 2
                    BATTERY_STATUS_DISCHARGING 3
                    BATTERY_STATUS_NOT_CHARGING 4
                    BATTERY_STATUS_FULL 5

                    BATTERY_PLUGGED_AC; // = 1
                    BATTERY_PLUGGED_USB; // = 2
                     **/
                }
            }, ifilter)
        }

        IntentFilter(Intent.ACTION_POWER_CONNECTED).let { ifilter ->
            batteryStatus = context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    isCharging = true
                    AppMessageBus.publish(
                        AppMessage(
                            APPMSG_TYPE.POWERSTATE_UPDATED,
                            getPowerState()
                        )
                    )
                }
            }, ifilter)
        }

        IntentFilter(Intent.ACTION_POWER_DISCONNECTED).let { ifilter ->
            batteryStatus = context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    isCharging = false
                    AppMessageBus.publish(
                        AppMessage(
                            APPMSG_TYPE.POWERSTATE_UPDATED,
                            getPowerState()
                        )
                    )
                }
            }, ifilter)
        }
    }
}