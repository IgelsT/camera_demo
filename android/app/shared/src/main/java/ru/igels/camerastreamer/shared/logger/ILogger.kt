package ru.igels.camerastreamer.shared.logger

interface ILogger {
    val logLevel: Int
    fun iLog(description: String, message: String)
    fun eLog(description: String, message: String, trace: String? = null)
    fun dLog(description: String, message: String)
}