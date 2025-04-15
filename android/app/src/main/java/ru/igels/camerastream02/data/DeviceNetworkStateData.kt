package ru.igels.camerastream02.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.*
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.data.appsettings.SettingsData
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppMessage
import java.io.File

class DeviceNetworkStateData private constructor(context: Context) {

    private val logTag = "DeviceNetworkState"
    private val connectivityManager: ConnectivityManager
    private var net = "NO_CONNECTION"
    private var pingCommand = "/system/bin/ping"
    private var pingCommandExist = false
    private var host = ""
    private var isNetConnected = false
    private var hostIsReachable = false
    private val pingInterval = 10 * 1000L
    private var pingJob: Job? = null
    private var runningDevice = ""

    companion object {
        private var instance: DeviceNetworkStateData? = null

        fun init(context: Context): DeviceNetworkStateData {
            if (instance == null) instance = DeviceNetworkStateData(context)
            return instance!!
        }

        fun getInstance(): DeviceNetworkStateData {
            checkIsInit()
            return instance!!
        }

        private fun checkIsInit() {
            if (instance == null) throw Exception("init() DeviceNetworkStateData first!")
        }

        fun getNetworkState(): Boolean {
            checkIsInit()
            return instance!!.isNetConnected
        }

        fun getHostState(): Boolean {
            checkIsInit()
            return instance!!.hostIsReachable
        }
    }

    init {
        iLog(logTag, "init")

        pingCommandExist = File(pingCommand).exists()
        runningDevice = AndroidInfoData.buildDeviceInfo().device.toString()

        AppMessageBus.publish(
            AppMessage(
                APPMSG_TYPE.NETWORKSTATE_UPDATED,
                Pair(isNetConnected, hostIsReachable)
            )
        )
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (NonCancellable.isActive) {
                val isNetConnected = isNetworkConnected()
                if (isNetConnected) setState(isNetConnected, if (pingCommandExist) ping() else true)
                else setState(netState = false, hostState = false)
                delay(pingInterval)
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {
            val networkRequest: NetworkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            connectivityManager.requestNetwork(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
//                    setState(true, hostIsReachable)
                        iLog(logTag, "requestNetwork onAvailable")
                        super.onAvailable(network)
                    }

                    override fun onUnavailable() {
//                    setState(netState = false, hostState = false)
                        iLog(logTag, "requestNetwork onUnavailable")
                        super.onUnavailable()
                    }

                    override fun onLost(network: Network) {
//                    setState(netState = false, hostState = false)
                        iLog(logTag, "requestNetwork onLost")
                        super.onLost(network)
                    }

                    override fun onLosing(network: Network, maxMsToLive: Int) {
//                    setState(netState = false, hostState = false)
                        iLog(logTag, "requestNetwork onLosing")
                        super.onLosing(network, maxMsToLive)
                    }
                })
        }
    }

    private fun setState(netState: Boolean, hostState: Boolean) {
        if (isNetConnected != netState || hostIsReachable != hostState) {
            isNetConnected = netState
            hostIsReachable = hostState
            AppMessageBus.publish(
                AppMessage(
                    APPMSG_TYPE.NETWORKSTATE_UPDATED,
                    Pair(isNetConnected, hostIsReachable)
                )
            )
            if (!isNetConnected || !hostIsReachable) {
                eLog(logTag, "Server unreachable! Network ${isNetConnected}, host ${hostIsReachable}")
            }
        }
    }

    private fun ping(): Boolean {
        val runtime = Runtime.getRuntime()
        if (isNetConnected) {
            if(runningDevice == "generic_x86_64") return true //android emulator ping not work
            host = SettingsData.getInstance().settings.baseUrl
            net = getNetworkType()
//            val hostAddress = InetAddress.getByName(host).hostAddress
            try {
                val pb = ProcessBuilder(pingCommand, "-c", "1", host)
                pb.redirectErrorStream(true)
                val p = pb.start()
                val exitValue = p.waitFor()
//                val ipProcess = runtime.exec("/system/bin/ping -c 1 $host")
//                val exitValue = ipProcess.waitFor()
//                val os = ipProcess.outputStream
                return exitValue == 0
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun isNetworkConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    @Suppress("DEPRECATION")
    private fun getNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.typeName ?: "NO_CONNECTION"
    }

    protected fun finalize() {
        pingJob?.cancel()
    }

}