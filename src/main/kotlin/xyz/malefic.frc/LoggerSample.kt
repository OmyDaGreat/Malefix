package xyz.malefic.frc

import co.touchlab.kermit.Logger

object LoggerSample {
    val logger = Logger.withTag("MyLibrary")

    fun logMessage(message: String) {
        logger.i { message }
    }
}
