package ru.igels.camerastream02.domain.logger

import android.annotation.SuppressLint
import android.util.Log
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.domain.models.LOGTYPE
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileController(private val logPath: String) {
    val logTag = "FileController"
    private val archPath = logPath + "arch"
    private val logToFile = true
    private var filename = ""
    private var myFile: File? = null
    private val lastLogsCount = 3

    init {
        startLog()
    }

    fun startLog() {
        myFile = null
        checkIfFolderExistAndCreate(logPath)
        checkIfFolderExistAndCreate(archPath)
        archiveOldLog()
        clearArchive()
        openFile()
    }

    @SuppressLint("SimpleDateFormat")
    private fun openFile() {
        if (myFile != null) return
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val currentDate = sdf.format(Date()).toString()
        filename = "${logPath}CamStreamerLog_$currentDate"
        try {
            myFile = File("$filename.log")
            if (!myFile!!.exists()) {
                myFile!!.createNewFile()
                Log.i(logTag, "Log file created")
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error create log file $filename", e)
            myFile = null
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Synchronized
    fun writeToFile(description: String, message: String, logType: LOGTYPE = LOGTYPE.INFO) {
        checkIfFolderExistAndCreate(logPath)
        checkIfFolderExistAndCreate(archPath)
        if (myFile == null) startLog()
        try {
            val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            val date = formatter.format(Date()).toString()
            val writeString = "$date [${logType}] $description: $message".trimIndent()
            myFile?.appendText(writeString)
            myFile?.appendText("\r\n")
//            logRotate()
        } catch (e: IOException) {
            Log.e(logTag, "File write failed: $e")
        }
    }

    fun getLogList(): List<String> {
        try {
            val archFolder = checkIfFolderExist(archPath) ?: return emptyList()
            val logFolder = checkIfFolderExist(logPath) ?: return emptyList()
            val archFiles = archFolder.listFiles()
            val logs = mutableListOf<String>()
            if (myFile != null)
                logs.add(myFile!!.name)

            for (log in archFiles!!) {
                logs.add(log.name)
            }
            return logs
        } catch (e: Exception) {
            Log.e(logTag, "error get log list: $e")
        }
        return emptyList()
    }

    fun getFile(fileName: String): File? {
        try {
            val logFile = File(logPath + fileName)
            if (logFile.exists()) return logFile

            val archFile = File("$archPath/$fileName")
            if (archFile.exists()) return archFile
        }
        catch (e: Exception) {
            Log.e(logTag, "error get log file: $e")
        }
        return null
    }

    private fun checkIfFolderExistAndCreate(path: String): File? {
        val folder = File(path)
        try {
            if (folder.exists()) return folder
            if (folder.mkdirs()) {
                Log.i(logTag, "Folder $path Created")
                return folder
            }
            Log.e(logTag, "Folder is $path NOT Created!")
        } catch (e: Exception) {
            Log.e(logTag, "Error while check path ${path}, ${e.message}")
        }
        return null
    }

    private fun checkIfFolderExist(path: String): File? {
        val folder = File(path)
        try {
            if (folder.exists()) return folder
        } catch (e: Exception) {
            Log.e(logTag, "Error while check path ${path}, ${e.message}")
        }
        return null
    }

    private fun archiveOldLog() {
        val logFolder = checkIfFolderExist(logPath) ?: return
        val archFolder = checkIfFolderExist(archPath) ?: return
        try {
            for (child in logFolder.listFiles()!!) {
                if (child.exists() && child.isFile) {
                    val zipFile = archFolder.absolutePath + "/" + child.name + ".zip"
                    zipFile(child.absolutePath, zipFile)
                    if (!child.delete()) {
                        Log.e(logTag, "error delete filename $child")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error archive old logs")
        }
    }

    private fun clearArchive() {
        try {
            val archFolder = checkIfFolderExist(archPath) ?: return
            val archFiles = archFolder.listFiles()
            val logFiles =
                archFiles?.filter { el -> !el.name.contains("crash") }
                    ?.sortedByDescending { it.lastModified() }
            val crashFiles = archFiles?.filter { el -> el.name.contains("crash") }
                ?.sortedByDescending { it.lastModified() }

            if (logFiles != null && logFiles.size > lastLogsCount) {
                for (i in lastLogsCount until logFiles.size) {
                    logFiles[i].delete()
                }
            }

            if (crashFiles != null && crashFiles.size > lastLogsCount) {
                for (i in lastLogsCount until crashFiles.size) {
                    crashFiles[i].delete()
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Error clear archive")
        }
    }

    private fun zipFile(input: String, output: String): Boolean {
        val inputFile = File(input)
        try {
            val fos = FileOutputStream(output)
            val zipOut = ZipOutputStream(fos)
            val fis = FileInputStream(inputFile)
            val zipEntry = ZipEntry(inputFile.name)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(1024)
            var length: Int
            while (fis.read(bytes).also { length = it } >= 0) {
                zipOut.write(bytes, 0, length)
            }
            zipOut.close()
            fis.close()
            fos.close()
            return true
        } catch (e: Exception) {
            Log.e(logTag, "Error zip file $input")
            return false
        }
    }

    fun markLastAsCrash() {
        if (myFile == null || !PermissionData.storagePermission) return
        try {
            val archFolder = checkIfFolderExist(archPath) ?: return
            val zipFile = archFolder.absolutePath + "/crash_" + myFile!!.name + ".zip"
            zipFile(myFile!!.absolutePath, zipFile)
            myFile!!.delete()
            myFile = null
        } catch (e: Exception) {
            Log.e(logTag, "Error make crash archive")
        }
    }
}