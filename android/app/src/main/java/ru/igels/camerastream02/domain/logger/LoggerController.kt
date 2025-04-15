package ru.igels.camerastream02.domain.logger

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppMessage
import ru.igels.camerastream02.domain.models.LOGTYPE
import java.io.*
import java.util.*

class LoggerController private constructor(
    private var logPath: String,
    private var appLogPrefix: String
) {

    private var appMessageFlowHandler: Job? = null
    private var today = Calendar.getInstance()
    private var fileController: FileController? = null

    companion object {
        private const val logTag: String = "LoggerController"
        private var instance: LoggerController? = null

        fun init(logPath: String, appLogPrefix: String): LoggerController {
            if (instance == null) instance = LoggerController(logPath, appLogPrefix)
            return instance!!
        }

        private fun checkIsInit(): LoggerController? {
            if (instance == null) throw Exception("init() LoggerController first!")
            return instance
        }

        fun log(
            description: String,
            message: String,
            logType: LOGTYPE = LOGTYPE.INFO,
            toFile: Boolean = true,
            trace: String?
        ) {
            checkIsInit()?.log(description, message, logType, toFile, trace)
        }

        fun enableFileLog() {

        }

        fun markLastAsCrash() {
            iLog(logTag, "mark las log as crash")
            checkIsInit()?.fileController?.markLastAsCrash()
        }

//        fun getLastCrash(): String? {
//            checkIsInit()
//            return instance!!.getLastCrash()
//        }
//
//        fun deleteLog(fileName: String) {
//            checkIsInit()
//            return instance!!.deleteFile(fileName)
//        }

        fun getLogList(): List<String> = checkIsInit()!!.fileController?.getLogList() ?: emptyList()
        fun getFile(fileName: String): File? = checkIsInit()?.fileController?.getFile(fileName)
    }

    init {
        Log.i(logTag, "Init logClass")
        if (PermissionData.storagePermission) {
            fileLogEnable()
        } else {
            appMessageFlowHandler = CoroutineScope(Dispatchers.IO).launch {
                AppMessageBus.getAppMessageBusFlow().filter {
                    it.type == APPMSG_TYPE.PERMISSION_UPDATED
                }.collect {
                    fileLogEnable()
                }
            }
        }
    }

    private fun fileLogEnable() {
        if (PermissionData.storagePermission) {
            fileController = FileController(logPath)
            AppMessageBus.publish(AppMessage(APPMSG_TYPE.LOG_ROTATED, null))
        }
    }

    fun log(
        description: String,
        message: String,
        logType: LOGTYPE = LOGTYPE.INFO,
        toFile: Boolean = true,
        trace: String?
    ) {
        when (logType) {
            LOGTYPE.INFO -> Log.i("${appLogPrefix}_${description}", message)
            LOGTYPE.DEBUG -> Log.d("${appLogPrefix}_${description}", message)
            LOGTYPE.ERROR -> Log.e("${appLogPrefix}_${description}", message)
        }
        if (toFile) {
            fileController?.writeToFile(description, message, logType)
            logRotate()
        }
    }

    private fun logRotate() {
        val curDate = Calendar.getInstance()
        curDate.add(Calendar.DAY_OF_YEAR, -1)
        if (today.get(Calendar.DAY_OF_YEAR) == curDate.get(Calendar.DAY_OF_YEAR)
            && curDate.get(Calendar.HOUR_OF_DAY) > 1
        ) {
            today = Calendar.getInstance()
            fileController?.startLog()
            log("LOG", "log rotated", LOGTYPE.INFO, true, null)
            AppMessageBus.publish(AppMessage(APPMSG_TYPE.LOG_ROTATED, null))
        }
    }


    //    fun getLastCrash(): String? {
//        val archFolder = checkDir(archPath)
//        if (!PermissionData.getStoragePermission() || archFolder == null) return null
//        try {
//            val archFiles = archFolder.listFiles() ?: return null
//            Arrays.sort(archFiles) { o1, o2 -> (if (o1.lastModified() > o2.lastModified()) o1.lastModified() else o2.lastModified()).toInt() }
//            for (file in archFiles) {
//                if (file.name.startsWith("crash")) return file.absolutePath
//            }
//        } catch (e: Exception) {
//            eLog(logTag, "Error get last crash")
//        }
//        return null
//    }
//
//    private fun deleteFile(fName: String) {
//        val archFolder = checkDir(archPath) ?: return
//        val fileName = archFolder.absoluteFile.toString() + "/" + fName
//        iLog(logTag, "Try to delete $fileName")
//        try {
//            val file = File(fileName)
//            if (file.exists()) file.delete()
//        } catch (e: Exception) {
//            eLog(logTag, "error delete file.asString")
//        }
//    }
//
    fun finalize() {
        appMessageFlowHandler?.cancel()
        Log.i(logTag, "Finalize log class")
    }
}