package ru.igels.camerastream02.data;

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
import ru.igels.camerastream02.MainApp
import ru.igels.camerastreamer.shared.models.DeviceInfoModel

object AndroidInfoData {
    @SuppressLint("HardwareIds")
    fun getAndroidID(): String {
        val value = Settings.Secure.getString(
            MainApp.getInstance().applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return value ?: pseudoID()
    }

    private fun pseudoID(): String {
        val value = "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
                Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 + Build.TYPE.length % 10 +
                Build.USER.length % 10
        return value
    }

    fun buildDeviceInfo(): DeviceInfoModel {
        return DeviceInfoModel(
            osversion = System.getProperty("os.version"),
            versionsdk = Build.VERSION.SDK,
            serial = Build.SERIAL,
            android_id = getAndroidID(),
            id = Build.ID,
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            type = Build.TYPE,
            user = Build.USER,
            version_codes = Build.VERSION_CODES.BASE,
            host = Build.HOST,
            fingerprint = Build.FINGERPRINT,
            version_release = Build.VERSION.RELEASE,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            board = Build.BOARD,
            cpu_abi = Build.CPU_ABI,
            display = Build.DISPLAY,
            hardware = Build.HARDWARE,
        )
        /**       Need phone state permission!!!
        //        val serviceName: String = Context.TELEPHONY_SERVICE
        //        val m_telephonyManager = MainApp.getInstance().applicationContext.getSystemService(serviceName) as TelephonyManager?
        //        val IMEI: String
        //        val IMSI: String
        //        IMEI = m_telephonyManager!!.deviceId
        //        IMSI = m_telephonyManager!!.subscriberId
         **/
    }
}
