package ru.igels.camerastream02.domain.logger

import ru.igels.camerastream02.domain.models.LOGTYPE
import ru.igels.camerastreamer.shared.logger.ILogger

object ExternalLogger: ILogger {
    override val logLevel: Int = 3

    override fun iLog(description: String, message: String) {
        ru.igels.camerastream02.domain.logger.iLog(description, message)
    }

    override fun eLog(description: String, message: String, trace: String?) {
        ru.igels.camerastream02.domain.logger.eLog(description, message, true, trace)
    }

    override fun dLog(description: String, message: String) {
        ru.igels.camerastream02.domain.logger.dLog(description, message)
    }
}

fun iLog(description: String, message: String, toFile: Boolean = true) {
    LoggerController.log(description, message, LOGTYPE.INFO, toFile, null)
}

fun eLog(description: String, message: String, toFile: Boolean = true, trace: String? = null) {
    LoggerController.log(description, message, LOGTYPE.ERROR, toFile, trace)
}

fun dLog(description: String, message: String, toFile: Boolean = false) {
    LoggerController.log(description, message, LOGTYPE.DEBUG, toFile, null)
}