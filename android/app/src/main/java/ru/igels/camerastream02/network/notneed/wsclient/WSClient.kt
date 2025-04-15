//package ru.igels.camerastream02.network.notneed.wsclient
//
//import android.app.Service
//import android.content.Intent
//import android.os.Handler
//import android.os.IBinder
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import kotlinx.coroutines.Job
//import okhttp3.*
//import okio.ByteString
//import ru.igels.camerastream02.data.AndroidInfoData
//import ru.igels.camerastream02.data.DevicePowerStateData
//import ru.igels.camerastream02.data.appsettings.SettingsData
//import ru.igels.camerastream02.domain.eLog
//import ru.igels.camerastream02.domain.iLog
//import ru.igels.camerastream02.network.models.FileUpload
//import ru.igels.camerastream02.utilites.Utils
//import java.io.File
//import java.io.IOException
//import java.util.concurrent.ArrayBlockingQueue
//
//
//class WSClient : Service() {
//
//    private val logTag = "WSClientService"
////    private val settingsCtrl = MainApp.getInstance().getSettingsController()
//    private var settings = SettingsData.getSettings()
//    private val RECONNECT_INTERVAL = 10 // in sec
//    private val CLOSE_STATUS = 1000
//    private val wsHandler: Handler = Utils.getThread("WSClientThread")
//    private val mClient: OkHttpClient = OkHttpClient()
//    private var request: Request? = null
//    private var webSocket: WebSocket? = null
//    private var isStarted = false
//    private var settingsFlowHandler: Job? = null
//    private var serverUrl = settings.baseUrl
//    private val serverPort = 9000
//    private var userName = settings.userName
//    private var userPassword = settings.userPassword
//    private var isConnected = false
//    private var powerStatus = DevicePowerStateData.getPowerState()
//    private var lastPing = System.currentTimeMillis()
//    private val PING_INTERVAL = 1000 * 30
////    private var pm: PowerManager? = null
////    private var wl: PowerManager.WakeLock? = null
////    var wifiManager: WifiManager? = null
////    var mWifiLock: WifiManager.WifiLock? = null
//
//    companion object {
//        private val logTag = "WSClientService"
//
//        val messagesQueue = ArrayBlockingQueue<BasePacket>(10)
//        val fileQueue = ArrayBlockingQueue<FileUpload>(10)
//
//        fun addToMessageQueue(packet: BasePacket) {
//            try {
//                messagesQueue.add(packet)
//            } catch (e: Exception) {
//                eLog(logTag, "message queue is full")
//            }
//        }
//
//        fun addToFileQueue(file: FileUpload) {
//            try {
//                fileQueue.add(file)
//            } catch (e: Exception) {
//                eLog(logTag, "sending queue is full")
//            }
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        iLog(logTag, "onCreate ${Thread.currentThread().name}")
////        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
////        wl = pm?.newWakeLock(
////            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
////            "myApp:notificationLock"
////        )
////        wl?.acquire() //set your time in milliseconds
////
////        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
////        mWifiLock =
////            wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "myApp:wifiLock");
////        mWifiLock?.acquire();
//
//        isStarted = true
//        isConnected = false
//        wsHandler.post {
//            connect()
//        }
//
////        settingsFlowHandler = CoroutineScope(Dispatchers.IO).launch {
////            SettingsData.getSettingsFlow().collect {
////                settings = it
////                if (it.baseUrl != serverUrl || it.userName != userName || it.userPassword != userPassword) {
////                    serverUrl = it.baseUrl
////                    userName = it.userName
////                    userPassword = it.userPassword
////                    stop()
////                    isStarted = true
////                    wsHandler.post {
////                        connect()
////                    }
////                }
////                Actions.sendDeviceState()
////            }
////        }
////        ping()
////        startForeground(1, makeNotification(this))
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val action = intent?.action ?: ""
//        if (action == "sendCamerasList") {
//            Actions.sendCamerasList()
//        }
//        return START_STICKY;
//    }
//
//
//    fun stop() {
//        isStarted = false
//        isConnected = false
//        closeConnection()
//    }
//
//    private fun closeConnection() {
//        try {
//            webSocket?.cancel()
//            webSocket?.close(CLOSE_STATUS, "")
//        } catch (e: Exception) {
//            eLog(logTag, "error close connection ${e.message}")
//        }
//        isConnected = false
//        request = null
//        webSocket = null
//    }
//
//    @Synchronized
//    private fun connect() {
//        if (serverUrl.isNotEmpty() && userName.isNotEmpty() && userPassword.isNotEmpty()) {
//            if (request != null) return
//            iLog(logTag, "Trying connecting to ws://${serverUrl}:${serverPort}")
//            try {
//                request = Request.Builder().url("ws://${serverUrl}:${serverPort}").build()
//                webSocket = mClient.newWebSocket(request, wsCallback)
//                isConnected = true
//            } catch (e: Exception) {
//                eLog(logTag, "error connect to ${serverUrl}:${serverPort}: ${e.message}")
//            }
//        } else {
//            eLog(logTag, "error connect to ws server params wrong")
//            stop()
//        }
//    }
//
//    fun reconnectAfterTime() {
//        closeConnection()
//        iLog(logTag, "Wait $RECONNECT_INTERVAL sec. to reconnect")
////        wifiManager?.reconnect()
//        if (isStarted)
//            wsHandler.postDelayed({
//                connect()
//            }, RECONNECT_INTERVAL * 1000L)
//    }
//
//    fun parseMessage(message: String) {
////        iLog(logTag, "<---- get message ${message}")
//        var jo: JsonObject? = null
//        try {
//            jo = Gson().fromJson(message, JsonObject::class.java)
//        } catch (e: Exception) {
//            eLog(logTag, "get message unknown format $message")
////            addToMessageQueue(makeResponse("", Result(false, "Error read json")))
//            return
//        }
//
//        var packet = BasePacket()
//
//        try {
//            packet = inPacketBuilder(jo)
//        } catch (e: Exception) {
//            eLog(logTag, "error build packet $e")
//        }
//
//        try {
//            if (packet is FromServerResponseModel || packet is FromServerRequestModel) {
//                when (packet.action) {
//                    "login" -> Actions.login(packet)
//                    "deviceInfo" -> Actions.sendDeviceInfo(packet)
//                    "deviceState" -> Actions.sendDeviceState(packet)
//                    "camerasList" -> Actions.sendCamerasList(packet)
//                    "setParams" -> Actions.setParams(packet)
//                    "deviceReboot" -> Actions.deviceReboot()
//                    "ping" -> Actions.ping(packet)
//                    "requestLog" -> Actions.requestLog(packet)
//                    else -> {
//                        eLog(logTag, "action ${packet.action} not found")
//                    }
//                }
//            } else {
//                iLog(logTag, "info packet")
//            }
//        }
//        catch (e: Exception) {
//            eLog(logTag, "error execute action $e")
//        }
//    }
//
//    private fun askQueue() {
//        if (powerStatus != DevicePowerStateData.getPowerState()) {
//            powerStatus = DevicePowerStateData.getPowerState()
//            Actions.sendDeviceState()
//        }
//        if (System.currentTimeMillis() - lastPing >= PING_INTERVAL) {
//            lastPing = System.currentTimeMillis()
//            Actions.ping()
//        }
//
//        if (!messagesQueue.isEmpty()) {
//            val packet = messagesQueue.peek()
//            if (packet != null && sendMessage(packet)) messagesQueue.poll()
//        }
//
//        if (!fileQueue.isEmpty()) {
//            val packet = fileQueue.peek()
//            if (packet != null && sendFile(packet)) fileQueue.poll()
//        }
//
//        if (isConnected) wsHandler.postDelayed({ askQueue() }, 1000)
//    }
//
//    private fun sendMessage(message: BasePacket): Boolean {
//        val jsonString = Gson().toJson(message)
//        try {
////            iLog(logTag, "----> send message ${jsonString}")
//            webSocket?.send(jsonString)
//            return true
//        } catch (e: Exception) {
//            eLog(logTag, "Error send ws message ${e.message}")
//            return false
//        }
//    }
//
//    private fun sendFile(file: FileUpload): Boolean {
//        iLog(logTag, "send file ${file.filename} to ${file.url}")
//        try {
//            val data = "{\"device_id\" : \"${AndroidInfoData.getAndroidID()}\"}"
//            val zipFile = File(file.filename)
//            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart(
//                    "file", zipFile.name,
//                    RequestBody.create(MediaType.parse("application/zip"), zipFile)
//                )
//                .addFormDataPart("action", "sendlog")
//                .addFormDataPart("data", data)
//                .build()
//
//            val request: Request = Request.Builder()
//                .url(file.url)
//                .post(requestBody)
//                .addHeader("Authorization", SettingsData.userToken)
//                .build()
//
//            mClient.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    eLog(logTag, "Error send file")
//                }
//
//                @Throws(IOException::class)
//                override fun onResponse(call: Call, response: Response) {
//                    if (!response.isSuccessful) {
//                        eLog(logTag, "Error send file")
//                    }
//                    var jo: JsonObject? = null
//                    try {
//                        jo = Gson().fromJson(response.body()?.string(), JsonObject::class.java)
//                        iLog(logTag, "OK send file")
//                    } catch (e: java.lang.Exception) {
//                        eLog(logTag, "Error send file")
//                    }
//                }
//            })
//            return true
//        } catch (ex: java.lang.Exception) {
//            eLog(logTag, "Error send file")
//        }
//        return true
//    }
//
////--------------------- CALLBACK ---------------------------------------
//
//    private val wsCallback = object : WebSocketListener() {
//        override fun onOpen(webSocket: WebSocket, response: Response) {
//            iLog(logTag, "Connected to ws://${serverUrl}:${serverPort}")
//            wsHandler.post {
//                sendMessage(makeRequest("login"))
//                askQueue()
//            }
//        }
//
//        override fun onMessage(webSocket: WebSocket, message: String) {
//            wsHandler.post {
//                parseMessage(message)
//            }
//        }
//
//        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//            iLog(logTag, "Receive Bytes : " + bytes.hex())
//        }
//
//        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//            iLog(logTag, "Closing Socket : $code / $reason")
//            reconnectAfterTime()
//        }
//
//        override fun onFailure(webSocket: WebSocket, throwable: Throwable, response: Response?) {
//            eLog(
//                logTag,
//                "error connect to ${serverUrl}:${serverPort}: ${throwable.message} code: ${response?.code()}"
//            )
//            reconnectAfterTime()
//        }
//    }
//
////--------------------- CALLBACK ---------------------------------------
//
//    override fun onBind(intent: Intent?): IBinder? {
//        TODO("Not yet implemented")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        iLog(logTag, "WSClient destroyed")
//        settingsFlowHandler?.cancel()
//        Utils.closeThread(wsHandler)
//    }
//}