package xyz.malefic

import kotlin.test.Test
import kotlin.test.assertEquals

class LoggerSampleTest {
    @Test
    fun logger_tag_remains_constant() {
        LoggerSample.logMessage("test")
        assertEquals("MyLibrary", LoggerSample.logger.tag)
    }
}
